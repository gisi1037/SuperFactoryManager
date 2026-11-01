package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class StateTypeCodecTest {

    @Test
    void everyPersistableShapeRoundTripsWithExactCanonicalIdentity() {
        Map<String, DataType> fields = new LinkedHashMap<>();
        fields.put("z", DataType.STRING);
        fields.put("Rows", DataType.array(DataType.dynamicArray(DataType.INTEGER), 2));
        DataType record = DataType.struct(fields);
        for (DataType type : Arrays.asList(
            DataType.INTEGER,
            DataType.BOOLEAN,
            DataType.STRING,
            DataType.struct(Collections.emptyMap()),
            DataType.array(DataType.INTEGER, 0),
            DataType.array(DataType.BOOLEAN, 256),
            record,
            DataType.dynamicArray(record),
            DataType.dynamicArray(DataType.dynamicArray(DataType.STRING)))) {
            DataType parsed = StateTypeCodec.parse(type.getSignature());
            assertEquals(type, parsed);
            assertEquals(type.getSignature(), parsed.getSignature());
            assertEquals(type.getDepth(), parsed.getDepth());
            assertEquals(type.getCellCount(), parsed.getCellCount());
            assertTrue(parsed.isPersistable());
        }
        assertTrue(
            DataType.dynamicArray(DataType.INTEGER)
                .isPersistable());
        assertFalse(
            DataType.dynamicArray(DataType.LABEL_REF)
                .isPersistable());
    }

    @Test
    void opaqueAndNoncanonicalDescriptorsAreRejectedWithoutSourceGrammarFallbacks() {
        for (String source : Arrays.asList(
            "",
            "integer",
            " INTEGER",
            "INTEGER ",
            "INTEGER[]",
            "VOID",
            "LABEL_REF",
            "RESOURCE_SELECTOR",
            "ITEM_KEY",
            "FLUID_KEY",
            "ITEM_CONTENTS",
            "ITEM_LOCATIONS",
            "FLUID_CONTENTS",
            "DYNAMIC_ARRAY[LABEL_REF]",
            "ARRAY[0:ITEM_KEY]",
            "ARRAY[01:INTEGER]",
            "ARRAY[-1:INTEGER]",
            "ARRAY[257:INTEGER]",
            "ARRAY[256:ARRAY[256:INTEGER]]",
            "ARRAY[9999999999999999999999:INTEGER]",
            "DYNAMIC_ARRAY[INTEGER",
            "ARRAY[:INTEGER]",
            "STRUCT{b:INTEGER;a:BOOLEAN;}",
            "STRUCT{a:INTEGER;a:BOOLEAN;}",
            "STRUCT{a:INTEGER}",
            "STRUCT{a:INTEGER;;}",
            "STRUCT{a-b:INTEGER;}",
            "STRUCT{é:INTEGER;}",
            "STRUCT{a:LABEL_REF;}",
            "INTEGERBOOLEAN",
            "INTEGER\0",
            "DYNAMIC_ARRAY[INTEGER]ignored"))
            assertThrows(IllegalArgumentException.class, () -> StateTypeCodec.parse(source), source);
        assertThrows(IllegalArgumentException.class, () -> StateTypeCodec.parse(null));
    }

    @Test
    void depthWidthAndDescriptorLengthHaveExplicitInclusiveBounds() {
        DataType deepest = DataType.INTEGER;
        for (int i = 0; i < 31; i++) deepest = DataType.dynamicArray(deepest);
        assertEquals(
            32,
            StateTypeCodec.parse(deepest.getSignature())
                .getDepth());
        String excessiveDepth = "DYNAMIC_ARRAY[" + deepest.getSignature() + "]";
        assertThrows(IllegalArgumentException.class, () -> StateTypeCodec.parse(excessiveDepth));
        StringBuilder fields = new StringBuilder("STRUCT{");
        for (int i = 0; i < 256; i++) fields.append(String.format(java.util.Locale.ROOT, "f%03d:INTEGER;", i));
        String maximumWidth = fields + "}";
        assertEquals(
            256,
            StateTypeCodec.parse(maximumWidth)
                .getLength());
        String excessiveWidth = fields + "f256:INTEGER;}";
        assertThrows(IllegalArgumentException.class, () -> StateTypeCodec.parse(excessiveWidth));
        char[] name = new char[StateTypeCodec.MAX_DESCRIPTOR_LENGTH - "STRUCT{:INTEGER;}".length()];
        Arrays.fill(name, 'a');
        String boundary = "STRUCT{" + new String(name) + ":INTEGER;}";
        assertEquals(30000, boundary.length());
        assertEquals(
            boundary,
            StateTypeCodec.parse(boundary)
                .getSignature());
        assertThrows(IllegalArgumentException.class, () -> StateTypeCodec.parse(boundary + " "));
    }
}
