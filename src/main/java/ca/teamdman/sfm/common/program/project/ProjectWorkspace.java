package ca.teamdman.sfm.common.program.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/**
 * One world-owned authoring service. The server submits immutable inputs and drains completions; only the dedicated
 * worker touches project files or compiles drafts. No Minecraft world, inventory or controller object crosses that
 * boundary. These provisional caps bound queued work, not a measured storage performance guarantee.
 *
 * File bytes are forced before atomic replacement. Directory entries/world saves are not one power-loss transaction;
 * back up world/sfm alongside the rest of the world. Publication stores source bundles; disk activation remains
 * separate.
 */
public final class ProjectWorkspace implements AutoCloseable {

    public static final int MAX_SOURCE_BYTES = 30000, MAX_NAME_BYTES = 96;
    public static final int MAX_PROJECTS_PER_OWNER = 128, PAGE_SIZE = 32, MAX_PENDING = 32;
    // Independent outstanding limits reserve progress for authoring AND cold activation. Count until callback drain.
    public static final int MAX_AUTHORING_PENDING = 24, MAX_EXECUTION_PENDING = 8;
    public static final int MAX_WAITING_EXECUTIONS = 4096, MAX_EXECUTION_REGISTRATIONS = 64;
    public static final int MAX_ADMISSION_INSPECTIONS = 8, MAX_EXECUTION_STARTS = 8;
    public static final int MAX_DIAGNOSTIC_CHARS = 1024;
    public static final int MAX_PUBLICATIONS_PER_PROJECT = 128, MAX_PROGRAMS_PER_WORLD = 4096;
    public static final long MAX_PROGRAM_BYTES_PER_WORLD = 64L * 1024 * 1024;
    public static final int MAX_EXECUTION_GRANTS_PER_WORLD = 4096;
    public static final long MAX_EXECUTION_GRANT_BYTES_PER_WORLD = 2L * 1024 * 1024;
    private final ProjectRepository repository;
    private final ThreadPoolExecutor worker;
    private final ArrayBlockingQueue<Runnable> callbacks = new ArrayBlockingQueue<>(MAX_PENDING);
    private boolean closed;
    private int pending, authoringPending, executionPending;
    // Server-owned FIFO. A request key is the manager's exact activation-generation token, never a coordinate.
    private final LinkedHashMap<Object, ExecutionWaiter> waitingExecutions = new LinkedHashMap<>();
    private final Map<Object, ExecutionWaiter> activeExecutions = new HashMap<>();
    private int registrations, lastAdmissionInspections, lastAdmissionSubmissions;

    public ProjectWorkspace(Path worldDirectory) {
        repository = new ProjectRepository(Objects.requireNonNull(worldDirectory, "worldDirectory"));
        worker = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING),
            task -> {
                Thread thread = new Thread(task, "SFM project storage");
                thread.setDaemon(true);
                return thread;
            });
    }

    /** Owner must come from the authenticated server player, never a request payload. */
    public boolean create(UUID owner, String name, String source, Consumer<ProjectResult<ProjectDraft>> callback) {
        return submit(() -> repository.create(owner, name, source), callback);
    }

    public boolean load(UUID owner, UUID id, Consumer<ProjectResult<ProjectDraft>> callback) {
        return submit(() -> repository.load(owner, id), callback);
    }

    public boolean save(UUID owner, UUID id, long expectedRevision, String name, String source,
        Consumer<ProjectResult<ProjectDraft>> callback) {
        return submit(() -> repository.save(owner, id, expectedRevision, name, source), callback);
    }

    public boolean list(UUID owner, int offset, Consumer<ProjectResult<ProjectPage>> callback) {
        return submit(() -> repository.list(owner, offset), callback);
    }

    /** Publish only an exact saved revision. Owner must be the authenticated server player. */
    public boolean publish(UUID owner, UUID id, long expectedRevision,
        Consumer<ProjectResult<PublicationReceipt>> callback) {
        return submit(() -> repository.publish(owner, id, expectedRevision), callback);
    }

    public boolean listPublications(UUID owner, UUID id, int offset,
        Consumer<ProjectResult<PublicationPage>> callback) {
        return submit(() -> repository.listPublications(owner, id, offset), callback);
    }

    /** Owner is the authenticated player. Selection must identify an already published exact build. */
    public boolean issueExecutionGrant(UUID owner, UUID id, long publishedRevision, String expectedDigest,
        Consumer<ProjectResult<ExecutionReference>> callback) {
        return submit(() -> repository.issueExecutionGrant(owner, id, publishedRevision, expectedDigest), callback);
    }

    /**
     * Worker-only cold activation; every success owns a freshly compiled Program and performs current storage checks.
     */
    public boolean prepareExecution(ExecutionReference reference, Consumer<ProjectResult<PreparedExecution>> callback) {
        return submit(() -> repository.prepareExecution(reference), callback, true);
    }

    /**
     * Register one cold request without submitting storage work. Called on the owning server thread; current is
     * evaluated only there immediately before admission. False means temporary waiter/registration capacity, not a
     * broken disk. Continuously valid enrolled requests are FIFO; overflow retries are paced by the manager.
     */
    public synchronized boolean enqueueExecution(Object key, ExecutionReference reference, BooleanSupplier current,
        Consumer<ProjectResult<PreparedExecution>> callback) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(callback, "callback");
        if (closed || registrations >= MAX_EXECUTION_REGISTRATIONS) return false;
        registrations++; // Count unsuccessful capacity attempts too, before doing registry/fence work.
        if (waitingExecutions.size() >= MAX_WAITING_EXECUTIONS) return false;
        if (waitingExecutions.containsKey(key) || activeExecutions.containsKey(key))
            throw new IllegalStateException("Execution request is already registered");
        waitingExecutions.put(key, new ExecutionWaiter(key, reference, current, callback));
        return true;
    }

    /** Waiting cancellation is O(1). Submitted work retains its outstanding slot until its real completion drains. */
    public synchronized void cancelExecution(Object key) {
        ExecutionWaiter waiter = waitingExecutions.remove(key);
        if (waiter == null) waiter = activeExecutions.remove(key);
        if (waiter != null) waiter.release();
    }

    public synchronized int getWaitingExecutionCount() {
        return waitingExecutions.size();
    }

    public synchronized int getPendingExecutionCount() {
        return executionPending;
    }

    public synchronized int getPendingAuthoringCount() {
        return authoringPending;
    }

    public synchronized int getLastAdmissionInspections() {
        return lastAdmissionInspections;
    }

    public synchronized int getLastAdmissionSubmissions() {
        return lastAdmissionSubmissions;
    }

    private RuntimeException admitExecutions() {
        RuntimeException failure = null;
        lastAdmissionInspections = lastAdmissionSubmissions = 0;
        while (!closed && !waitingExecutions.isEmpty()
            && executionPending < MAX_EXECUTION_PENDING
            && lastAdmissionInspections < MAX_ADMISSION_INSPECTIONS
            && lastAdmissionSubmissions < MAX_EXECUTION_STARTS) {
            java.util.Iterator<ExecutionWaiter> iterator = waitingExecutions.values()
                .iterator();
            ExecutionWaiter waiter = iterator.next();
            lastAdmissionInspections++;
            boolean current;
            try {
                current = waiter.current.getAsBoolean();
            } catch (RuntimeException error) {
                current = false;
            }
            if (!current) {
                iterator.remove();
                Consumer<ProjectResult<PreparedExecution>> callback = waiter.callback;
                waiter.release();
                try {
                    callback.accept(ProjectResult.failure(ProjectResult.Status.STALE, "Reference activation changed"));
                } catch (RuntimeException error) {
                    if (failure == null) failure = error;
                }
                continue;
            }
            lastAdmissionSubmissions++;
            if (!prepareExecution(waiter.reference, result -> finishExecution(waiter, result))) break;
            iterator.remove();
            activeExecutions.put(waiter.key, waiter);
        }
        return failure;
    }

    private void finishExecution(ExecutionWaiter waiter, ProjectResult<PreparedExecution> result) {
        // Already on the server callback drain, after the outstanding worker slot has been retired.
        if (waiter.key == null) return;
        activeExecutions.remove(waiter.key);
        Consumer<ProjectResult<PreparedExecution>> callback = waiter.callback;
        waiter.release();
        callback.accept(result);
    }

    private static final class ExecutionWaiter {

        Object key;
        final ExecutionReference reference;
        BooleanSupplier current;
        Consumer<ProjectResult<PreparedExecution>> callback;

        ExecutionWaiter(Object key, ExecutionReference reference, BooleanSupplier current,
            Consumer<ProjectResult<PreparedExecution>> callback) {
            this.key = key;
            this.reference = reference;
            this.current = current;
            this.callback = callback;
        }

        void release() {
            key = null;
            current = null;
            callback = null;
        }
    }

    public boolean validate(UUID owner, UUID id, long expectedRevision,
        Consumer<ProjectResult<ProjectValidation>> callback) {
        return submit(() -> {
            ProjectDraft draft = repository.load(owner, id);
            if (draft.revision != expectedRevision) throw new ProjectRepository.Failure(
                ProjectResult.Status.STALE,
                "Project changed; reload before validating");
            ProgramBuildResult result = new ProgramBuilder(draft.source).build();
            // Discard the temporary compiled plan: validation grants no run/install authority.
            return new ProjectValidation(draft, result.isSuccess(), diagnostic(result.getErrorMessage()));
        }, callback);
    }

    /**
     * Return false if the service is closed or all outstanding callback slots are occupied. Rejected submissions have
     * no callback and no side effects; the server can immediately report busy/closed to that request.
     */
    private <T> boolean submit(Operation<T> operation, Consumer<ProjectResult<T>> callback) {
        return submit(operation, callback, false);
    }

    private synchronized <T> boolean submit(Operation<T> operation, Consumer<ProjectResult<T>> callback,
        boolean execution) {
        Objects.requireNonNull(callback, "callback");
        if (closed || pending >= MAX_PENDING
            || (execution ? executionPending >= MAX_EXECUTION_PENDING : authoringPending >= MAX_AUTHORING_PENDING))
            return false;
        pending++;
        if (execution) executionPending++;
        else authoringPending++;
        worker.execute(() -> {
            ProjectResult<T> result;
            try {
                synchronized (repository.transactionLock()) {
                    // A replacement workspace uses the same lock until this old transaction has published.
                    // Interruption alone cannot cancel an atomic filesystem move.
                    synchronized (ProjectWorkspace.this) {
                        if (closed) return;
                    }
                    result = ProjectResult.success(operation.run());
                }
            } catch (ProjectRepository.Failure error) {
                result = ProjectResult.failure(error.status, error.getMessage());
            } catch (IllegalArgumentException error) {
                result = ProjectResult.failure(ProjectResult.Status.INVALID, diagnostic(error.getMessage()));
            } catch (IOException | RuntimeException error) {
                // Do not leak absolute paths, private source or native filesystem error text to clients.
                result = ProjectResult.failure(ProjectResult.Status.IO_ERROR, "Project storage request failed");
            }
            ProjectResult<T> completed = result;
            synchronized (ProjectWorkspace.this) {
                if (!closed) callbacks.add(() -> {
                    if (execution) executionPending--;
                    else authoringPending--;
                    callback.accept(completed);
                });
            }
        });
        return true;
    }

    /** Call on the owning server thread, with a finite per-tick completion allowance. */
    public int drainCallbacks(int maximum) {
        if (maximum < 0 || maximum > MAX_PENDING) throw new IllegalArgumentException("Invalid completion allowance");
        int drained = 0;
        RuntimeException failure = null;
        while (drained < maximum) {
            synchronized (this) {
                if (closed) break;
                Runnable callback = callbacks.poll();
                if (callback == null) break;
                pending--;
                // close() cannot return while an already-drained callback is about to touch its old world.
                try {
                    callback.run();
                } catch (RuntimeException error) {
                    if (failure == null) failure = error;
                }
            }
            drained++;
        }
        synchronized (this) {
            try {
                if (!closed && maximum > 0) {
                    RuntimeException admissionFailure = admitExecutions();
                    if (failure == null) failure = admissionFailure;
                }
            } finally {
                registrations = 0;
            }
        }
        // Finish bounded drainage/admission first, then report one failure to the owning service's logger.
        if (failure != null) throw failure;
        return drained;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        for (ExecutionWaiter waiter : waitingExecutions.values()) waiter.release();
        for (ExecutionWaiter waiter : activeExecutions.values()) waiter.release();
        waitingExecutions.clear();
        activeExecutions.clear();
        callbacks.clear();
        pending = authoringPending = executionPending = 0;
        worker.shutdownNow();
    }

    static String diagnostic(String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length() && result.length() < MAX_DIAGNOSTIC_CHARS; i++) {
            char value = text.charAt(i);
            if (value == '\u00a7' || Character.isISOControl(value) && value != '\n' && value != '\t')
                result.append(' ');
            else if (Character.isHighSurrogate(value)) {
                if (i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))
                    && result.length() + 2 <= MAX_DIAGNOSTIC_CHARS) {
                    result.append(value)
                        .append(text.charAt(++i));
                } else result.append('?');
            } else result.append(Character.isLowSurrogate(value) ? '?' : value);
        }
        return result.toString();
    }

    private interface Operation<T> {

        T run() throws IOException;
    }
}
