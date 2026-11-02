package ca.teamdman.sfml.data.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Shared immutable inspection index, with server-thread admission accounting. No recipe/native callbacks on lookup. */
public final class RecipeCatalog {

    // GTNH 2.9's macerator alone has 24,993 native entries. Retained bytes remain independently capped.
    public static final int MAX_MAPS = 16, MAX_RECIPES = 65536, MAX_POSTINGS = 131072;
    public static final int MAX_RESULTS = 64, WORK_PER_TICK = 256;
    public static final long MAX_RETAINED_BYTES = 256L * 1024 * 1024;

    public enum Status {
        MATCHES,
        NO_MATCH,
        UNSUPPORTED,
        LIMIT_EXCEEDED,
        STALE,
        NOT_READY
    }

    public static final class Result {

        public final Status status;
        public final List<RecipeDescriptor> recipes;
        public final boolean coverageComplete;
        private final List<Token> references;

        private Result(Status status, List<RecipeDescriptor> recipes, boolean complete, List<Token> references) {
            this.status = status;
            this.recipes = recipes;
            coverageComplete = complete;
            this.references = references;
        }

        public boolean isAmbiguous() {
            return recipes.size() > 1;
        }

        /** References belong to the same admitted observation, even if the catalog is later withdrawn. */
        public Token reference(int index) {
            return references.get(index);
        }
    }

    private static final Result[] EMPTY_RESULTS;
    static {
        EMPTY_RESULTS = new Result[Status.values().length];
        for (Status status : Status.values()) EMPTY_RESULTS[status.ordinal()] = new Result(
            status,
            Collections.emptyList(),
            status == Status.NO_MATCH,
            Collections.emptyList());
    }

    /** An inspection reference, not authorization to transfer, craft, or access another player's source. */
    public static final class Token {

        public final UUID session;
        public final long revision;
        public final String adapter, map, fingerprint;

        public Token(UUID session, long revision, String adapter, String map, String fingerprint) {
            this.session = Objects.requireNonNull(session, "Catalog session");
            if (revision <= 0) throw new IllegalArgumentException("Invalid catalog revision");
            this.revision = revision;
            this.adapter = RecipeDescriptor.text(adapter, false);
            this.map = RecipeDescriptor.text(map, false);
            if (fingerprint == null || fingerprint.length() != 64)
                throw new IllegalArgumentException("Invalid recipe fingerprint");
            for (int i = 0; i < fingerprint.length(); i++) {
                char c = fingerprint.charAt(i);
                if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f'))
                    throw new IllegalArgumentException("Invalid recipe fingerprint");
            }
            this.fingerprint = fingerprint;
        }
    }

    private static final class Bucket {

        private final List<RecipeDescriptor> mutable = new ArrayList<>();
        private List<RecipeDescriptor> recipes;
        private boolean unsupported;

        void freeze() {
            recipes = Collections.unmodifiableList(mutable);
        }
    }

    private static final class MapData {

        private final Map<String, Bucket> outputs = new HashMap<>();
        private final Map<String, RecipeDescriptor> descriptors = new LinkedHashMap<>();
        private boolean incomplete;
    }

    private final UUID session;
    private final long revision, ownerThread;
    private final Map<String, MapData> maps;
    private final long retainedBytes;
    private final int recipes, postings;
    private long tick = Long.MIN_VALUE;
    private int work;

    private RecipeCatalog(Builder builder) {
        session = builder.session;
        revision = builder.revision;
        ownerThread = Thread.currentThread()
            .getId();
        maps = Collections.unmodifiableMap(builder.maps);
        retainedBytes = builder.retainedBytes;
        recipes = builder.recipes;
        postings = builder.postings;
    }

    public int recipeCount() {
        return recipes;
    }

    public int postingCount() {
        return postings;
    }

    /** Conservative retained-size accounting, not a measured JVM heap size. */
    public long retainedBytes() {
        return retainedBytes;
    }

    public Result find(ItemIdentity output, String map, QueryBudget budget, long serverTick) {
        Objects.requireNonNull(output, "Exact recipe output");
        RecipeDescriptor.text(map, false);
        Objects.requireNonNull(budget, "Recipe query budget");
        if (!admit(serverTick, 1)) return result(Status.LIMIT_EXCEEDED);
        budget.chargeSlots(map.length() + 1);
        MapData data = maps.get(map);
        if (data == null) return result(Status.UNSUPPORTED);
        String fingerprint = hex(output.fingerprint(budget));
        Bucket bucket = data.outputs.get(fingerprint);
        if (bucket == null) return result(data.incomplete ? Status.UNSUPPORTED : Status.NO_MATCH);
        int count = bucket.recipes.size();
        if (count > MAX_RESULTS || !admit(serverTick, count)) return result(Status.LIMIT_EXCEEDED);
        budget.chargeSlots(count + 1);
        boolean complete = !data.incomplete && !bucket.unsupported;
        // Candidate admission covers its immutable inspection reference as well. No later minting round trip.
        budget.chargeAllocation(2 + count);
        List<Token> references = new ArrayList<>(count);
        for (RecipeDescriptor descriptor : bucket.recipes) {
            budget.chargeSlots(descriptor.adapter.length() + descriptor.map.length() + 65);
            references.add(new Token(session, revision, descriptor.adapter, descriptor.map, descriptor.fingerprint));
        }
        return new Result(
            complete ? Status.MATCHES : Status.UNSUPPORTED,
            bucket.recipes,
            complete,
            Collections.unmodifiableList(references));
    }

    public Token token(RecipeDescriptor recipe, QueryBudget budget, long serverTick) {
        Objects.requireNonNull(recipe, "Recipe descriptor");
        Objects.requireNonNull(budget, "Recipe token budget");
        if (!admit(serverTick, 1)) throw new IllegalArgumentException("Recipe catalog tick budget exhausted");
        budget.chargeSlots(recipe.map.length() + 65);
        MapData data = maps.get(recipe.map);
        if (data == null || data.descriptors.get(recipe.fingerprint) != recipe)
            throw new IllegalArgumentException("Recipe is not from this catalog");
        budget.chargeAllocation(1);
        return new Token(session, revision, recipe.adapter, recipe.map, recipe.fingerprint);
    }

    public Result resolve(Token token, QueryBudget budget, long serverTick) {
        Objects.requireNonNull(token, "Recipe token");
        Objects.requireNonNull(budget, "Recipe query budget");
        if (!admit(serverTick, 1)) return result(Status.LIMIT_EXCEEDED);
        budget.chargeSlots(65 + token.map.length() + token.adapter.length());
        if (!session.equals(token.session) || revision != token.revision) return result(Status.STALE);
        MapData data = maps.get(token.map);
        if (data == null) return result(Status.STALE);
        RecipeDescriptor descriptor = data.descriptors.get(token.fingerprint);
        if (descriptor == null || !descriptor.adapter.equals(token.adapter)) return result(Status.STALE);
        budget.chargeAllocation(2);
        boolean complete = !data.incomplete && descriptor.unsupportedRequirements.isEmpty();
        return new Result(
            complete ? Status.MATCHES : Status.UNSUPPORTED,
            Collections.singletonList(descriptor),
            complete,
            Collections.singletonList(token));
    }

    private boolean admit(long serverTick, int units) {
        if (Thread.currentThread()
            .getId() != ownerThread) throw new IllegalStateException("Recipe queries require their server thread");
        if (serverTick != tick) {
            tick = serverTick;
            work = 0;
        }
        if (units > WORK_PER_TICK - work) return false;
        work += units;
        return true;
    }

    private static Result result(Status status) {
        return EMPTY_RESULTS[status.ordinal()];
    }

    public static Result unavailable(Status status) {
        if (status == Status.MATCHES || status == Status.NO_MATCH)
            throw new IllegalArgumentException("Unavailable catalog cannot establish matches or absence");
        return result(status);
    }

    private static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder(64);
        for (byte value : bytes) {
            text.append(Character.forDigit((value >>> 4) & 15, 16));
            text.append(Character.forDigit(value & 15, 16));
        }
        return text.toString();
    }

    /** A failed/capped build can never publish partial authoritative maps. Exact duplicate descriptors coalesce. */
    public static final class Builder {

        private final UUID session;
        private final long revision;
        private final Map<String, MapData> maps = new LinkedHashMap<>();
        private long retainedBytes = 1024;
        private int recipes, postings, visits;
        private boolean closed;

        public Builder(UUID session, long revision) {
            this.session = Objects.requireNonNull(session, "Catalog session");
            if (revision <= 0) throw new IllegalArgumentException("Invalid catalog revision");
            this.revision = revision;
        }

        public void registerMap(String map) {
            open();
            try {
                RecipeDescriptor.text(map, false);
                if (maps.containsKey(map)) return;
                reserve(1024 + map.length() * 2L, 0);
                if (maps.size() == MAX_MAPS) fail("maps", maps.size(), 1, MAX_MAPS);
                maps.put(map, new MapData());
            } catch (RuntimeException failure) {
                closed = true;
                throw failure;
            }
        }

        public void incomplete(String map) {
            data(map).incomplete = true;
        }

        public void unsupported(String map, List<ItemIdentity> outputs, QueryBudget budget) {
            open();
            try {
                MapData data = data(map);
                visit();
                if (outputs == null || outputs.size() > RecipeDescriptor.MAX_SLOTS)
                    fail("output slots", 0, outputs == null ? -1 : outputs.size(), RecipeDescriptor.MAX_SLOTS);
                if (outputs.isEmpty()) {
                    data.incomplete = true;
                    return;
                }
                for (ItemIdentity key : outputs) {
                    String fingerprint = hex(
                        Objects.requireNonNull(key, "Output identity")
                            .fingerprint(budget));
                    if (!data.outputs.containsKey(fingerprint)) reserve(512, 1);
                    data.outputs.computeIfAbsent(fingerprint, ignored -> new Bucket()).unsupported = true;
                }
            } catch (RuntimeException failure) {
                closed = true;
                throw failure;
            }
        }

        public void add(RecipeDescriptor descriptor, QueryBudget budget) {
            open();
            try {
                Objects.requireNonNull(descriptor, "Recipe descriptor");
                MapData data = data(descriptor.map);
                visit();
                if (data.descriptors.containsKey(descriptor.fingerprint)) return;
                Set<String> outputs = new HashSet<>();
                for (RecipeDescriptor.ItemPart part : descriptor.itemOutputs)
                    outputs.add(hex(part.item.fingerprint(budget)));
                reserve(Math.addExact(descriptor.retainedBytes(), 512L + outputs.size() * 512L), outputs.size());
                data.descriptors.put(descriptor.fingerprint, descriptor);
                recipes++;
                for (String output : outputs) {
                    Bucket bucket = data.outputs.computeIfAbsent(output, ignored -> new Bucket());
                    bucket.mutable.add(descriptor);
                    bucket.unsupported |= !descriptor.unsupportedRequirements.isEmpty();
                }
            } catch (RuntimeException failure) {
                closed = true;
                throw failure;
            }
        }

        public RecipeCatalog finish() {
            open();
            closed = true;
            for (MapData data : maps.values()) for (Bucket bucket : data.outputs.values()) bucket.freeze();
            return new RecipeCatalog(this);
        }

        private MapData data(String map) {
            open();
            MapData data = maps.get(map);
            if (data == null) {
                closed = true;
                throw new IllegalArgumentException("Recipe map not registered");
            }
            return data;
        }

        private void visit() {
            if (++visits > MAX_RECIPES) fail("visits", visits - 1, 1, MAX_RECIPES);
        }

        private void reserve(long bytes, int nextPostings) {
            if (bytes < 0 || bytes > MAX_RETAINED_BYTES - retainedBytes)
                fail("retained bytes", retainedBytes, bytes, MAX_RETAINED_BYTES);
            if (nextPostings > MAX_POSTINGS - postings) fail("postings", postings, nextPostings, MAX_POSTINGS);
            retainedBytes += bytes;
            postings += nextPostings;
        }

        private void open() {
            if (closed) throw new IllegalStateException("Recipe catalog builder is closed");
        }

        private void fail(String kind, long used, long requested, long limit) {
            closed = true;
            throw new IllegalArgumentException(
                "Recipe catalog " + kind
                    + " limit: used "
                    + used
                    + ", requested "
                    + requested
                    + ", limit "
                    + limit
                    + "; descriptors "
                    + recipes
                    + ", postings "
                    + postings);
        }
    }
}
