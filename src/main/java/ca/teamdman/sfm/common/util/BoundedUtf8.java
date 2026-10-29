package ca.teamdman.sfm.common.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Strict encoding at document boundaries; invalid Unicode must never change silently on save. */
public final class BoundedUtf8 {

    private BoundedUtf8() {}

    public static ByteBuffer encode(String text, int maximum) {
        // Each UTF-16 code unit needs at least one UTF-8 byte for a well-formed string.
        if (text == null || text.length() > maximum) throw new IllegalArgumentException("Text exceeds the byte limit");
        try {
            ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(text));
            if (encoded.remaining() > maximum) throw new IllegalArgumentException("Text exceeds the byte limit");
            return encoded;
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Text contains invalid Unicode", error);
        }
    }
}
