package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SyntaxRegistry {

    private static final List<SyntaxSnippet> ALL_SNIPPETS = new ArrayList<>();
    private static final Map<SnippetCategory, List<SyntaxSnippet>> CATEGORY_MAP = new LinkedHashMap<>();

    static {
        for (SnippetCategory cat : SnippetCategory.values()) CATEGORY_MAP.put(cat, new ArrayList<>());

        snippet(
            "trig_timer",
            "EVERY <20> TICKS",
            SnippetCategory.TRIGGER,
            "EVERY <20> TICKS DO\n  <statements>\nEND",
            "Runs every N ticks; 20 ticks is one second");
        snippet(
            "trig_seconds",
            "EVERY <seconds> SECONDS",
            SnippetCategory.TRIGGER,
            "EVERY <seconds> SECONDS DO\n  <statements>\nEND",
            "Schedules an interval measured in seconds");
        snippet(
            "trig_global",
            "Global timer with offset",
            SnippetCategory.TRIGGER,
            "EVERY <20> GLOBAL + <offset> TICKS DO\n  <statements>\nEND",
            "Uses the global clock with a fixed offset");
        snippet(
            "trig_pulse",
            "EVERY REDSTONE PULSE",
            SnippetCategory.TRIGGER,
            "EVERY REDSTONE PULSE DO\n  <statements>\nEND",
            "Existing manager pulse trigger; use FROM for selected native Redstone Ports");
        snippet(
            "trig_port_rising",
            "Named Redstone Port RISING edge",
            SnippetCategory.TRIGGER,
            "EVERY REDSTONE RISING FROM <sensor> NORTH SIDE DO\n  <statements>\nEND",
            "Runs when the selected native-port input rises; use absolute faces, without slots or round robin");
        snippet(
            "trig_port_pulse",
            "Named Redstone Port PULSE edge",
            SnippetCategory.TRIGGER,
            "EVERY REDSTONE PULSE FROM <sensor> NORTH SIDE DO\n  <statements>\nEND",
            "PULSE FROM is an alias for a named native-port rising edge");
        snippet(
            "trig_port_falling",
            "Named Redstone Port FALLING edge",
            SnippetCategory.TRIGGER,
            "EVERY REDSTONE FALLING FROM <sensor> NORTH SIDE DO\n  <statements>\nEND",
            "Runs when the selected native-port input falls; missing/incomplete groups are unavailable");
        snippet(
            "trig_port_gate",
            "Native port edges drive an output",
            SnippetCategory.TRIGGER,
            "EVERY REDSTONE RISING FROM <sensor> NORTH SIDE DO\n  OUTPUT 15 REDSTONE LEVEL TO <emit> EAST SIDE\nEND\n"
                + "EVERY REDSTONE FALLING FROM <sensor> NORTH SIDE DO\n  OUTPUT 0 REDSTONE LEVEL TO <emit> EAST SIDE\nEND",
            "Complete program: named input edges switch an output face on a second Redstone Port");
        snippet(
            "trig_name",
            "NAME a program",
            SnippetCategory.TRIGGER,
            "NAME \"<name>\"",
            "Place before STATE declarations and triggers");

        snippet(
            "query_stock_helpers",
            "TOTAL / STOCK / SHORTAGE from one scan",
            SnippetCategory.QUERY,
            "LET $stock = LABEL(\"<src>\").contents()\nPRINT(\"total\", TOTAL($stock))\n"
                + "IF (LEN($stock) > 0) THEN\n  LET $have = STOCK($stock, $stock[0].item)\n"
                + "  PRINT(\"shortage\", SHORTAGE(<amount>, $have), \"locations\", TOTAL($stock[0].locations))\nEND",
            "Reads captured immutable values without querying inventories again; STOCK matches an exact item key");
        snippet(
            "io_transfer_all",
            "Transfer all items",
            SnippetCategory.IO,
            "transfer(label(\"<src>\"), label(\"<dst>\"))",
            "One bounded attempt for all currently visible source items; append INTO $result to inspect progress");
        snippet(
            "io_transfer_selected_all",
            "Transfer all matching items",
            SnippetCategory.IO,
            "transfer(label(\"<src>\"), label(\"<dst>\"), item(\"<item>\"))",
            "Defaults the amount to matching visible stock; destination capacity and access still apply");
        snippet(
            "io_transfer_values",
            "TRANSFER values with a result",
            SnippetCategory.IO,
            "TRANSFER (LABEL(\"<src>\"), LABEL(\"<dst>\"), ITEM(\"<item>\"), <amount>) INTO $result\n"
                + "PRINT($result.moved, $result.remaining, $result.status)",
            "Standalone action: moves up to one aggregate amount, using fresh live stock and access checks");
        snippet(
            "io_transfer_exact",
            "TRANSFER an exact scanned item",
            SnippetCategory.IO,
            "LET $source = LABEL(\"<src>\")\nLET $stock = $source.contents()\n"
                + "IF (LEN($stock) > 0) THEN\n  TRANSFER ($source, LABEL(\"<dst>\"), $stock[0].item, <amount>) INTO $result\n"
                + "  PRINT($result.moved, $result.remaining, $result.status)\nEND",
            "Exact ID, metadata and NBT identity; captured quantities are observations rather than reservations");
        snippet(
            "io_basic",
            "INPUT / OUTPUT",
            SnippetCategory.IO,
            "INPUT FROM <src>\nOUTPUT TO <dst>",
            "Registers sources, then attempts transfers into destinations");
        snippet(
            "io_input_all",
            "INPUT FROM label",
            SnippetCategory.IO,
            "INPUT FROM <src>",
            "Registers source views for subsequent OUTPUT statements");
        snippet(
            "io_input_qty",
            "INPUT quantity",
            SnippetCategory.IO,
            "INPUT <quantity> FROM <src>",
            "Limits the total input allowance across subsequent outputs");
        snippet(
            "io_input_with",
            "INPUT a specific item",
            SnippetCategory.IO,
            "INPUT <quantity> item:<item> FROM <src>",
            "Selects a registered item by namespace and name");
        snippet(
            "io_input_retain",
            "INPUT with source reserve",
            SnippetCategory.IO,
            "INPUT <quantity> RETAIN <amount> FROM <src>",
            "Leaves the retained amount in selected sources");
        snippet(
            "io_output_all",
            "OUTPUT TO label",
            SnippetCategory.IO,
            "OUTPUT TO <dst>",
            "Attempts transfers from the active input sources");
        snippet(
            "io_output_qty",
            "OUTPUT quantity",
            SnippetCategory.IO,
            "OUTPUT <quantity> TO <dst>",
            "Transfers up to this quantity using remaining input allowance");
        snippet(
            "io_output_retain",
            "OUTPUT toward target stock",
            SnippetCategory.IO,
            "OUTPUT RETAIN <amount> TO <dst>",
            "Fills destinations toward this total stock cap");
        snippet(
            "io_output_empty",
            "OUTPUT to empty slots",
            SnippetCategory.IO,
            "OUTPUT TO EMPTY SLOTS IN <dst>",
            "Inserts into empty item slots only");
        snippet(
            "io_redstone_out",
            "OUTPUT redstone level",
            SnippetCategory.IO,
            "OUTPUT <15> REDSTONE LEVEL TO <emit>",
            "Sets a native Redstone Port or legacy emitter output; literal levels retain historical 0-15 clamping");
        snippet(
            "io_redstone_dynamic",
            "Computed Redstone Port output",
            SnippetCategory.IO,
            "LET $level = <level>\nOUTPUT ($level) REDSTONE LEVEL TO <emit> EAST SIDE",
            "Integer expression evaluated once before output writes; dynamic levels must be within 0-15");
        snippet(
            "io_redstone_forward",
            "Forward native Redstone Port input",
            SnippetCategory.IO,
            "LET $sensor = LABEL(\"<sensor>\").side(\"north\")\nIF ($sensor.redstoneAvailable()) THEN\n"
                + "  LET $level = $sensor.redstone()\n  OUTPUT ($level) REDSTONE LEVEL TO <emit> EAST SIDE\nEND",
            "Checks native-group availability before forwarding its maximum input level to an output face");
        snippet(
            "io_forget",
            "FORGET a source",
            SnippetCategory.IO,
            "FORGET <label>",
            "Discards active INPUT registrations for this label");
        snippet(
            "io_forget_all",
            "FORGET all sources",
            SnippetCategory.IO,
            "FORGET",
            "Discards all active INPUT registrations");

        snippet(
            "cond_if",
            "IF / THEN / END",
            SnippetCategory.CONDITION,
            "IF <condition> THEN\n  <statements>\nEND",
            "Runs statements when the condition is true");
        snippet(
            "cond_if_else",
            "IF / ELSE",
            SnippetCategory.CONDITION,
            "IF <condition> THEN\n  <statements>\nELSE\n  <fallback>\nEND",
            "Chooses a true or false branch");
        snippet(
            "cond_else_if",
            "IF / ELSE IF / ELSE",
            SnippetCategory.CONDITION,
            "IF <condition> THEN\n  <statements>\nELSE IF <other_condition> THEN\n  <fallback>\nELSE\n  FORGET\nEND",
            "Tests alternatives in order");
        snippet(
            "loop_counted_for",
            "FOR index = start TO end STEP step",
            SnippetCategory.CONDITION,
            "FOR $i = <start> TO <end> STEP <step> DO\n  <statements>\nEND",
            "Inclusive integer range; STEP defaults to 1 when omitted; loop binding is read-only");
        snippet(
            "loop_array_for",
            "FOR value IN array",
            SnippetCategory.DATA,
            "FOR $value IN <array> DO\n  <statements>\nEND",
            "Iterates a snapshot of the array expression; each element binding is read-only");
        snippet(
            "loop_while",
            "WHILE condition DO",
            SnippetCategory.CONDITION,
            "WHILE (<condition>) DO\n  <statements>\nEND",
            "Rechecks the condition before each iteration; invocation work limits bound execution");
        snippet(
            "cond_has",
            "Label HAS item threshold",
            SnippetCategory.CONDITION,
            "<src> HAS GT <quantity> item:<item>",
            "Tests selected stock against a quantity threshold");
        snippet(
            "cond_has_overall",
            "OVERALL label HAS",
            SnippetCategory.CONDITION,
            "OVERALL <src> HAS GE <quantity>",
            "Compares combined stock across selected blocks");
        snippet(
            "cond_has_some",
            "SOME label HAS",
            SnippetCategory.CONDITION,
            "SOME <src> HAS GE <quantity>",
            "True when at least one selected block meets the threshold");
        snippet(
            "cond_has_every",
            "EVERY label HAS",
            SnippetCategory.CONDITION,
            "EVERY <src> HAS GE <quantity>",
            "Tests the threshold for every selected block");
        snippet(
            "cond_has_one",
            "ONE label HAS",
            SnippetCategory.CONDITION,
            "ONE <src> HAS GE <quantity>",
            "True when exactly one selected block meets the threshold");
        snippet(
            "cond_has_lone",
            "LONE label HAS",
            SnippetCategory.CONDITION,
            "LONE <src> HAS GE <quantity>",
            "True when at most one selected block meets the threshold, including zero");
        snippet(
            "cond_redstone_on",
            "REDSTONE > 0",
            SnippetCategory.CONDITION,
            "REDSTONE > 0",
            "Tests the manager or receiver redstone level");
        snippet(
            "cond_redstone_level",
            "REDSTONE >= level",
            SnippetCategory.CONDITION,
            "REDSTONE >= <level>",
            "Compares redstone against a level threshold");
        snippet("cond_not", "NOT condition", SnippetCategory.CONDITION, "NOT <condition>", "Negates a condition");
        snippet(
            "cond_and",
            "condition AND condition",
            SnippetCategory.CONDITION,
            "<condition> AND <other_condition>",
            "Both conditions must pass; evaluation short-circuits");
        snippet(
            "cond_or",
            "condition OR condition",
            SnippetCategory.CONDITION,
            "<condition> OR <other_condition>",
            "Either condition may pass; evaluation short-circuits");

        snippet(
            "label_typed_local",
            "LET a typed label",
            SnippetCategory.LABEL,
            "LET $src = LABEL(\"<label>\")",
            "Local exact label reference; cannot be stored in STATE");
        snippet(
            "label_each",
            "EACH label",
            SnippetCategory.LABEL,
            "EACH <label>",
            "Uses independent transfer allowances for each block");
        snippet(
            "label_group",
            "Multiple labels",
            SnippetCategory.LABEL,
            "<src>, <dst>",
            "Combines labeled block groups");
        for (String side : new String[] { "TOP", "BOTTOM", "NORTH", "SOUTH", "EAST", "WEST", "NULL" }) {
            snippet(
                "side_" + side.toLowerCase(Locale.ROOT),
                side + " SIDE",
                SnippetCategory.LABEL,
                "<label> " + side + " SIDE",
                side.equals("NULL") ? "Uses unsided access, subject to machine rules"
                    : "Uses the container's " + side.toLowerCase(Locale.ROOT) + " face");
        }
        snippet(
            "side_each",
            "EACH SIDE",
            SnippetCategory.LABEL,
            "<label> EACH SIDE",
            "Uses all faces without multiplying shared physical stock");

        snippet(
            "mod_slots",
            "SLOTS first-last",
            SnippetCategory.MODIFIER,
            "SLOTS <first>-<last>",
            "Restricts access to an inclusive zero-based slot range");
        snippet(
            "mod_slot_single",
            "SLOT index",
            SnippetCategory.MODIFIER,
            "SLOT <0>",
            "Restricts access to one zero-based backend slot");
        snippet(
            "mod_slot_set",
            "SLOTS range and individual slots",
            SnippetCategory.MODIFIER,
            "SLOTS 0-8, 12, 20-22",
            "Combines inclusive ranges and individual backend indices");
        snippet(
            "mod_retain",
            "RETAIN amount",
            SnippetCategory.MODIFIER,
            "RETAIN <amount>",
            "Input reserves source stock; output caps destination stock");
        snippet(
            "mod_with_tag",
            "WITH TAG ore name",
            SnippetCategory.MODIFIER,
            "WITH TAG <ore_tag>",
            "Filters items by a 1.7.10 OreDictionary name");
        snippet(
            "mod_without_tag",
            "WITHOUT TAG ore name",
            SnippetCategory.MODIFIER,
            "WITHOUT TAG <ore_tag>",
            "Excludes items matching an OreDictionary name");
        snippet(
            "mod_except",
            "EXCEPT item",
            SnippetCategory.MODIFIER,
            "EXCEPT item:<item>",
            "Exclusion follows resource limits, before FROM or TO");
        snippet(
            "mod_resource_each",
            "Quantity EACH / RETAIN EACH",
            SnippetCategory.MODIFIER,
            "<quantity> EACH RETAIN <amount> EACH",
            "Applies independent allowances and reserves per resource identity");
        snippet(
            "mod_round_block",
            "ROUND ROBIN BY BLOCK",
            SnippetCategory.MODIFIER,
            "ROUND ROBIN BY BLOCK",
            "Selects one block per resolution, even when it cannot accept items");
        snippet(
            "mod_round_label",
            "ROUND ROBIN BY LABEL",
            SnippetCategory.MODIFIER,
            "ROUND ROBIN BY LABEL",
            "Rotates label groups; place after labels and before sides/slots");
        snippet(
            "mod_resource_or",
            "Item alternatives with OR",
            SnippetCategory.MODIFIER,
            "item:minecraft:iron_ingot OR item:minecraft:gold_ingot",
            "Shares one allowance across matching resource alternatives");
        snippet(
            "mod_resource_limits",
            "Independent resource limits",
            SnippetCategory.MODIFIER,
            "8 item:minecraft:iron_ingot, 4 item:minecraft:gold_ingot",
            "Comma-separated limits each have their own allowance");
        snippet(
            "mod_metadata",
            "Item metadata selector",
            SnippetCategory.MODIFIER,
            "\"item:minecraft:wool:14\"",
            "Quoted resource selector for an item metadata value");

        snippet(
            "data_let",
            "LET mutable value",
            SnippetCategory.DATA,
            "LET $<name> = <value>",
            "Local inside a trigger/function; shared RAM value when declared before functions/triggers");
        snippet(
            "data_global_let",
            "LET program-wide memory",
            SnippetCategory.DATA,
            "LET $runs = 0\nEVERY 20 TICKS DO\n  $runs = $runs + 1\n  PRINT(\"runs\", $runs)\nEND",
            "Insert at program scope. Shared across runs; resets on reload/recompile; STATE persists to disk");
        snippet(
            "data_logic_aliases",
            "Logic: !, !=, ==, &&, ||",
            SnippetCategory.DATA,
            "LET $ready = TRUE\nLET $n = 8\nIF (!$ready || ($n != 0 && $n == 8)) THEN\n  <statements>\nEND",
            "Boolean aliases retain NOT/AND/OR precedence and short-circuit behavior; no automatic coercion");
        snippet(
            "data_nbt_compare",
            "Remember and compare exact item/NBT",
            SnippetCategory.DATA,
            "LET $baseline = LABEL(\"<label>\").contents()\nEVERY 20 TICKS DO\n"
                + "  LET $current = LABEL(\"<label>\").contents()\n"
                + "  IF (LEN($baseline) > 0 && LEN($current) > 0) THEN\n"
                + "    PRINT(\"item or NBT changed\", $current[0].item != $baseline[0].item)\n  END\nEND",
            "Insert at program scope with the label connected. Exact keys include metadata/full NBT, excluding count");
        snippet(
            "function_integer",
            "FUNCTION / typed parameters / RETURN",
            SnippetCategory.DATA,
            "FUNCTION math.double($value: INTEGER) RETURNS INTEGER DO\n  RETURN $value * 2\nEND\n"
                + "EVERY 20 TICKS DO\n  LET $result = math.double(3)\nEND",
            "Complete program; functions precede triggers and cannot capture STATE or execute I/O/PRINT");
        snippet(
            "function_boolean",
            "Boolean function",
            SnippetCategory.DATA,
            "FUNCTION logic.invert($value: BOOLEAN) RETURNS BOOLEAN DO\n  RETURN NOT $value\nEND\n"
                + "EVERY 20 TICKS DO\n  LET $result = logic.invert(FALSE)\nEND",
            "Typed boolean argument and return value; parameters are local to the call");
        snippet(
            "function_string",
            "String function",
            SnippetCategory.DATA,
            "FUNCTION text.join($first: STRING, $second: STRING) RETURNS STRING DO\n  RETURN $first + $second\nEND\n"
                + "EVERY 20 TICKS DO\n  LET $result = text.join(\"iron\", \"_ingot\")\nEND",
            "Pure typed string concatenation; function names are case-sensitive");
        snippet(
            "function_array_record",
            "Function with typed array of records",
            SnippetCategory.DATA,
            "FUNCTION stock.count($stacks: {item: STRING, count: INTEGER}[3]) RETURNS INTEGER DO\n"
                + "  LET $total = 0\n  FOR $stack IN $stacks DO\n    SET $total = $total + $stack.count\n  END\n  RETURN $total\nEND\n"
                + "EVERY 20 TICKS DO\n  LET $stacks = [{item: \"iron\", count: 1}, {item: \"gold\", count: 2}, {item: \"copper\", count: 3}]\n"
                + "  LET $total = stock.count($stacks)\nEND",
            "Fixed array length, fields and element types are part of the function signature");
        snippet(
            "data_state",
            "STATE persistent value",
            SnippetCategory.DATA,
            "STATE $<name> = <value>",
            "Place before triggers; value persists on the program disk");
        snippet(
            "data_set",
            "Assign an existing binding (SET optional)",
            SnippetCategory.DATA,
            "$<name> = <value>",
            "Assigns the same fixed type to a visible local, program-wide LET or STATE; SET remains accepted");
        snippet(
            "data_counter",
            "Persistent invocation counter",
            SnippetCategory.DATA,
            "STATE $runs = 0\nEVERY 20 TICKS DO\n  SET $runs = $runs + 1\nEND",
            "Complete program: counts invocations on the disk");
        snippet(
            "data_boolean",
            "Boolean local",
            SnippetCategory.DATA,
            "LET $enabled = TRUE",
            "Declares a boolean; FALSE and boolean expressions also work");
        snippet(
            "data_print",
            "PRINT values to debug terminal",
            SnippetCategory.DATA,
            "PRINT(\"value\", <value>)",
            "Records 1-8 values when terminal capture is on; disabled PRINT skips expressions");
        snippet(
            "data_quantity",
            "Computed transfer quantity",
            SnippetCategory.DATA,
            "LET $batch = <quantity>\nINPUT ($batch) FROM <src>\nOUTPUT ($batch) TO <dst>",
            "Data expressions in transfer limits require parentheses");
        snippet(
            "data_retention",
            "Computed source reserve",
            SnippetCategory.DATA,
            "LET $reserve = <amount>\nINPUT RETAIN ($reserve) FROM <src>",
            "Snapshots a computed retention value when the statement runs");
        snippet(
            "data_condition",
            "IF with a data expression",
            SnippetCategory.DATA,
            "LET $enabled = TRUE\nIF ($enabled) THEN\n  <statements>\nEND",
            "Data boolean conditions require parentheses");

        snippet(
            "array_literal",
            "Fixed array and index read",
            SnippetCategory.COLLECTION,
            "LET $values = [8, 16, 32]\nLET $first = $values[0]",
            "Arrays have fixed length and one element type; indices start at zero");
        snippet(
            "array_set",
            "Assign an array element",
            SnippetCategory.COLLECTION,
            "LET $values = [8, 16, 32]\nSET $values[0] = <value>",
            "Updates an existing index with the same element type");
        snippet(
            "array_index",
            "Computed array index",
            SnippetCategory.COLLECTION,
            "LET $values = [8, 16, 32]\nLET $index = 1\nLET $selected = $values[$index]",
            "Reads a bounded integer index; invalid indices fault");
        snippet(
            "struct_literal",
            "Struct and field read",
            SnippetCategory.COLLECTION,
            "LET $config = {batch: 8, reserve: 16}\nLET $batch = $config.batch",
            "Struct fields and field types are fixed at declaration");
        snippet(
            "struct_set",
            "Assign a struct field",
            SnippetCategory.COLLECTION,
            "LET $config = {batch: 8, reserve: 16}\nSET $config.batch = <value>",
            "Updates an existing field with the same type");
        snippet(
            "struct_nested",
            "Array of item records",
            SnippetCategory.COLLECTION,
            "LET $stacks = [{item: \"minecraft:iron_ingot\", count: 8}, {item: \"minecraft:gold_ingot\", count: 4}]\nSET $stacks[0].count = 16\nLET $id = $stacks[0].item",
            "Combines array indexing, field access and nested assignment");
        snippet(
            "array_value_copy",
            "Independent collection copy",
            SnippetCategory.COLLECTION,
            "LET $original = [8, 16]\nLET $copy = $original\nSET $copy[0] = 32",
            "Assignments use value semantics; original remains unchanged");

        snippet(
            "string_literal",
            "String local",
            SnippetCategory.STRING,
            "LET $text = \"<text>\"",
            "Text has its own fixed type; there is no implicit numeric conversion");
        snippet(
            "string_concat",
            "Concatenate strings with +",
            SnippetCategory.STRING,
            "LET $text = \"iron\" + \"_ingot\"",
            "Concatenation requires strings on both sides");
        snippet(
            "string_equal",
            "Case-sensitive string equality",
            SnippetCategory.STRING,
            "LET $text = \"iron\"\nIF ($text = \"iron\") THEN\n  <statements>\nEND",
            "String equality is case-sensitive; EQ is also accepted");
        snippet(
            "string_not_equal",
            "String inequality with !=",
            SnippetCategory.STRING,
            "LET $text = \"iron\"\nIF ($text != \"gold\") THEN\n  <statements>\nEND",
            "Negates equality; strings have no ordering comparison");
        snippet(
            "string_escapes",
            "Quoted text, backslash and line breaks",
            SnippetCategory.STRING,
            "LET $text = \"quote: \\\" slash: \\\\ line: \\n tab: \\t unicode: \\u0041\"",
            "Supported escapes include quotation mark, backslash, n, r, t, b, f and Unicode");
        snippet(
            "string_state",
            "Persistent string",
            SnippetCategory.STRING,
            "STATE $message = \"ready\"",
            "Persistent string declaration; place before triggers");

        snippet(
            "math_arithmetic",
            "Integer + - * / %",
            SnippetCategory.MATH,
            "LET $sum = 8 + 4\nLET $difference = 8 - 4\nLET $product = 8 * 4\nLET $quotient = 8 / 4\nLET $remainder = 8 % 3",
            "Signed 64-bit checked arithmetic; division truncates toward zero");
        snippet(
            "math_sign",
            "Unary signs and grouping",
            SnippetCategory.MATH,
            "LET $negative = -(8 + 4)\nLET $positive = +8",
            "Use parentheses to control expression order");
        snippet(
            "math_compare",
            "Numeric comparisons",
            SnippetCategory.MATH,
            "LET $n = <value>\nIF ($n >= 0 AND $n < 64) THEN\n  <statements>\nEND",
            "Compare with =, <, >, <=, >= or EQ/LT/GT/LE/GE");
        snippet(
            "math_bitwise",
            "Bitwise & | ^ ~",
            SnippetCategory.MATH,
            "LET $mask = 15\nLET $bits = ($mask & 7) | (8 ^ 2)\nLET $inverse = ~$mask",
            "Integer-only 64-bit AND, OR, XOR and complement");
        snippet(
            "math_shift",
            "Shift << >> >>>",
            SnippetCategory.MATH,
            "LET $n = 8\nLET $left = $n << 2\nLET $signed = $n >> 1\nLET $unsigned = $n >>> 1",
            "64-bit shifts; distance must be from 0 through 63");
        snippet(
            "math_flag_test",
            "Set, clear and test a bit",
            SnippetCategory.MATH,
            "LET $flags = 0\nSET $flags = $flags | (1 << 3)\nIF (($flags & (1 << 3)) > 0) THEN\n  SET $flags = $flags & ~(1 << 3)\nEND",
            "Uses a mask to set, inspect and clear one flag");
        snippet(
            "math_high_bit",
            "64-bit high bit",
            SnippetCategory.MATH,
            "LET $high = 1 << 63\nLET $low = $high >>> 63",
            "The sign bit yields the minimum signed 64-bit value; unsigned shift recovers one");

        snippet(
            "query_label",
            "LABEL literal constructor",
            SnippetCategory.QUERY,
            "LET $src = LABEL(\"<label>\")",
            "Exact literal label name; a local typed handle, never STATE");
        snippet(
            "query_item",
            "ITEM exact ID constructor",
            SnippetCategory.QUERY,
            "LET $item = ITEM(\"<item>\")",
            "Exact namespace:name literal; no item: prefix, tags or partial wildcards");
        snippet(
            "query_contents",
            "Scan grouped item contents",
            SnippetCategory.QUERY,
            "LET $stock = LABEL(\"<label>\").contents()",
            "Read-only exact item groups and physical locations; missing or over-budget views fault");
        snippet(
            "query_contents_filtered",
            "Scan ore-selected contents",
            SnippetCategory.QUERY,
            "LET $stock = LABEL(\"<label>\").contents(ORE(\"<ore_tag>\"))",
            "Ore membership filters the scan; groups still distinguish exact metadata and NBT");
        snippet(
            "query_contents_loop",
            "Inspect groups and locations",
            SnippetCategory.QUERY,
            "LET $stock = LABEL(\"<label>\").contents()\nFOR $group IN $stock DO\n"
                + "  PRINT($group.id, $group.metadata, $group.total)\n  FOR $where IN $group.locations DO\n"
                + "    PRINT($where.x, $where.y, $where.z, $where.slot, $where.amount)\n  END\nEND",
            "One captured observation, nested read-only iteration; no reservation or transfer");
        snippet(
            "query_ore",
            "ORE dictionary selector",
            SnippetCategory.QUERY,
            "LET $ore = ORE(\"<ore_tag>\")",
            "Exact case-sensitive ore membership, such as ingotIron; a RESOURCE_SELECTOR, not an item identity");
        snippet(
            "data_min_max",
            "MIN / MAX stock limit",
            SnippetCategory.MATH,
            "LET $smaller = MIN(<amount>, <quantity>)\nLET $larger = MAX(<amount>, <quantity>)",
            "Two signed integer expressions; MAX chooses the larger, arguments run once left to right");
        snippet(
            "data_clamp",
            "CLAMP integer range",
            SnippetCategory.MATH,
            "LET $bounded = CLAMP(<amount>, <minimum>, <maximum>)",
            "Bounds an integer inclusively; reversed bounds fault rather than being exchanged");
        snippet(
            "data_abs",
            "ABS integer magnitude",
            SnippetCategory.MATH,
            "LET $magnitude = ABS(<amount>)",
            "Checked signed integer magnitude; the minimum signed 64-bit value faults");
        snippet(
            "data_len",
            "LEN array or string",
            SnippetCategory.DATA,
            "LET $length = LEN(<array>)",
            "Top-level array element count or Unicode code-point count of a string");
        snippet(
            "query_all_items",
            "ITEM all items",
            SnippetCategory.QUERY,
            "LET $items = ITEM(\"*\")",
            "The single wildcard selects every item in an observation");
        snippet(
            "query_side",
            "Select a query side",
            SnippetCategory.QUERY,
            "LET $view = LABEL(\"<label>\").side(\"north\")",
            "Returns a new typed view for an absolute side");
        snippet(
            "query_slots",
            "Select a query slot range",
            SnippetCategory.QUERY,
            "LET $view = LABEL(\"<label>\").slots(<first>, <last>)",
            "Returns a new view with an inclusive zero-based slot range");
        snippet(
            "query_view",
            "Chain side and slots",
            SnippetCategory.QUERY,
            "LET $view = LABEL(\"<label>\").side(\"north\").slots(<first>, <last>)",
            "Composes immutable access restrictions for later observations");
        snippet(
            "cond_typed_count",
            "Count selected items",
            SnippetCategory.QUERY,
            "(LABEL(\"<label>\").count(ITEM(\"minecraft:iron_ingot\")) >= <quantity>)",
            "Counts exact visible stock; an unavailable observation faults");
        snippet(
            "cond_typed_empty",
            "Selected item stock is empty",
            SnippetCategory.QUERY,
            "(LABEL(\"<label>\").empty(ITEM(\"*\")))",
            "True for an available complete observation with zero selected items");
        snippet(
            "cond_typed_available",
            "Selected item observation available",
            SnippetCategory.QUERY,
            "(LABEL(\"<label>\").available(ITEM(\"*\")))",
            "Checks observation completeness, not stock or acceptance");
        snippet(
            "query_empty_all",
            "No-argument empty()",
            SnippetCategory.QUERY,
            "LET $isEmpty = LABEL(\"<label>\").empty()",
            "Observes emptiness across all visible items in this view");
        snippet(
            "query_available_all",
            "No-argument available()",
            SnippetCategory.QUERY,
            "LET $ready = LABEL(\"<label>\").available()",
            "Checks availability of the complete item view");
        snippet(
            "query_guarded_count",
            "Check availability before count",
            SnippetCategory.QUERY,
            "LET $src = LABEL(\"<label>\")\nLET $item = ITEM(\"minecraft:iron_ingot\")\nIF ($src.available($item) AND $src.count($item) >= <quantity>) THEN\n  <statements>\nEND",
            "Short-circuits count when the observation is unavailable");
        snippet(
            "query_redstone",
            "Redstone Port input level",
            SnippetCategory.QUERY,
            "LET $sensor = LABEL(\"<sensor>\").side(\"north\")\nLET $level = $sensor.redstone()",
            "Maximum native-port input over selected faces; missing/incomplete groups fault; slots are rejected");
        snippet(
            "query_powered",
            "Redstone Port powered state",
            SnippetCategory.QUERY,
            "LET $sensor = LABEL(\"<sensor>\").side(\"north\")\nLET $powered = $sensor.powered()",
            "True when the maximum native-port input is above zero; unavailable groups fault");
        snippet(
            "query_redstone_available",
            "Native redstone observation available",
            SnippetCategory.QUERY,
            "LET $ready = LABEL(\"<sensor>\").side(\"north\").redstoneAvailable()",
            "True only for a complete, available native-port group; independent of item availability");
        snippet(
            "function_redstone_query",
            "Function reads a Redstone Port",
            SnippetCategory.QUERY,
            "FUNCTION signals.read($sensor: LABEL_REF) RETURNS INTEGER DO\n  RETURN $sensor.redstone()\nEND\n"
                + "EVERY 20 TICKS DO\n  LET $sensor = LABEL(\"<sensor>\").side(\"north\")\n"
                + "  IF ($sensor.redstoneAvailable()) THEN\n    LET $level = signals.read($sensor)\n"
                + "    OUTPUT ($level) REDSTONE LEVEL TO <emit> EAST SIDE\n  END\nEND",
            "Complete program: a typed helper observes native input; its caller performs output");
    }

    private static void snippet(String id, String title, SnippetCategory category, String template,
        String description) {
        register(new SyntaxSnippet(id, title, category, template, description));
    }

    private static void register(SyntaxSnippet snippet) {
        ALL_SNIPPETS.add(snippet);
        List<SyntaxSnippet> list = CATEGORY_MAP.get(snippet.getCategory());
        if (list != null) {
            list.add(snippet);
        }
    }

    public static List<SyntaxSnippet> getAllSnippets() {
        return Collections.unmodifiableList(ALL_SNIPPETS);
    }

    public static List<SyntaxSnippet> getSnippetsForCategory(SnippetCategory cat) {
        List<SyntaxSnippet> list = CATEGORY_MAP.get(cat);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /**
     * Dynamically generates label snippets from disk labels.
     */
    public static List<SyntaxSnippet> getLabelSnippets(Set<String> diskLabels) {
        List<SyntaxSnippet> result = new ArrayList<>();
        if (diskLabels != null && !diskLabels.isEmpty()) {
            for (String label : diskLabels) {
                result.add(
                    new SyntaxSnippet(
                        "label_" + label,
                        label,
                        SnippetCategory.LABEL,
                        label,
                        "Configured disk label targeting blocks in world"));
            }
        } else {
            result.add(
                new SyntaxSnippet(
                    "label_placeholder",
                    "<label>",
                    SnippetCategory.LABEL,
                    "<label>",
                    "Name of a labeled block in cable network"));
        }
        return result;
    }

    /**
     * Determines the most relevant snippets based on the program text and cursor position.
     */
    public static List<SyntaxSnippet> getContextualSuggestions(String fullText, int cursorLine, int cursorCol,
        Set<String> diskLabels) {
        List<SyntaxSnippet> suggestions = new ArrayList<>();

        String[] lines = fullText.split("\n", -1);
        String currentLine = (cursorLine >= 0 && cursorLine < lines.length) ? lines[cursorLine] : "";
        String textBeforeCursor = currentLine.substring(0, Math.min(cursorCol, currentLine.length()))
            .trim();
        String upperBefore = textBeforeCursor.toUpperCase(Locale.ROOT);

        // Check if inside a trigger DO ... END
        boolean insideTrigger = false;
        int triggerDepth = 0;
        for (int i = 0; i < cursorLine; i++) {
            String l = lines[i].toUpperCase(Locale.ROOT);
            if (l.contains("DO") && !l.startsWith("#") && !l.startsWith("//")) triggerDepth++;
            if (l.contains("END") && !l.startsWith("#") && !l.startsWith("//"))
                triggerDepth = Math.max(0, triggerDepth - 1);
        }
        insideTrigger = triggerDepth > 0;

        // Context 1: After FROM or TO -> Suggest Labels & Sides
        if (upperBefore.endsWith("FROM") || upperBefore.endsWith("TO")
            || upperBefore.contains("FROM ")
            || upperBefore.contains("TO ")) {
            suggestions.addAll(getLabelSnippets(diskLabels));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.LABEL));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.MODIFIER));
            return filterOrDeduplicate(suggestions, textBeforeCursor);
        }

        // Context 2: After IF or ELSE IF -> Suggest Conditions
        if (upperBefore.endsWith("IF") || upperBefore.endsWith("THEN") || upperBefore.contains("IF ")) {
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.CONDITION));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.QUERY));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.MATH));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.STRING));
            suggestions.addAll(getLabelSnippets(diskLabels));
            return filterOrDeduplicate(suggestions, textBeforeCursor);
        }

        // Data declarations and expressions expose every implemented data feature.
        if (upperBefore.startsWith("LET ") || upperBefore.startsWith("SET ")
            || upperBefore.startsWith("STATE ")
            || textBeforeCursor.contains("$")) {
            for (SnippetCategory category : new SnippetCategory[] { SnippetCategory.DATA, SnippetCategory.COLLECTION,
                SnippetCategory.STRING, SnippetCategory.MATH, SnippetCategory.QUERY })
                suggestions.addAll(getSnippetsForCategory(category));
            return filterOrDeduplicate(suggestions, textBeforeCursor);
        }

        // Context 3: Outside any trigger -> Prioritize Triggers
        if (!insideTrigger) {
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.TRIGGER));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.DATA));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.STRING));
            suggestions.addAll(getSnippetsForCategory(SnippetCategory.IO));
            return filterOrDeduplicate(suggestions, textBeforeCursor);
        }

        // Context 4: Inside trigger -> Prioritize Statements (I/O, Conditions, Labels)
        suggestions.addAll(getSnippetsForCategory(SnippetCategory.IO));
        suggestions.addAll(getSnippetsForCategory(SnippetCategory.CONDITION));
        suggestions.addAll(getLabelSnippets(diskLabels));
        suggestions.addAll(getSnippetsForCategory(SnippetCategory.MODIFIER));
        suggestions.addAll(getSnippetsForCategory(SnippetCategory.TRIGGER));
        for (SnippetCategory category : new SnippetCategory[] { SnippetCategory.DATA, SnippetCategory.COLLECTION,
            SnippetCategory.STRING, SnippetCategory.MATH, SnippetCategory.QUERY })
            suggestions.addAll(getSnippetsForCategory(category));

        return filterOrDeduplicate(suggestions, textBeforeCursor);
    }

    private static List<SyntaxSnippet> filterOrDeduplicate(List<SyntaxSnippet> input, String textBeforeCursor) {
        String lastWord = getLastWord(textBeforeCursor).toUpperCase(Locale.ROOT);
        if (lastWord.isEmpty()) {
            return input;
        }

        List<SyntaxSnippet> filtered = new ArrayList<>();
        for (SyntaxSnippet s : input) {
            String titleUpper = s.getTitle()
                .toUpperCase(Locale.ROOT);
            String templUpper = s.getTemplate()
                .toUpperCase(Locale.ROOT);
            if (titleUpper.contains(lastWord) || templUpper.contains(lastWord)) {
                filtered.add(s);
            }
        }
        return filtered.isEmpty() ? input : filtered;
    }

    public static String getLastWord(String text) {
        if (text == null || text.isEmpty()) return "";
        int lastSpace = Math.max(text.lastIndexOf(' '), text.lastIndexOf('\t'));
        return lastSpace != -1 ? text.substring(lastSpace + 1) : text;
    }
}
