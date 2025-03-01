/*
 * @(#)FastDoubleParserTest.java
 * Copyright © 2022. Werner Randelshofer, Switzerland. MIT License.
 */

package io.questdb.std.fastdouble;

import io.questdb.std.Chars;
import io.questdb.std.MemoryTag;
import io.questdb.std.NumericException;
import io.questdb.std.Unsafe;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests class {@link FastDoubleParser}
 */
public class FastDoubleParserTest extends AbstractFastXParserTest {
    @Test
    public void testParseDoubleBitsByteArrayIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> testBits(t, u -> FastDoubleParser.parseDoubleBits(u.input().getBytes(StandardCharsets.UTF_8), u.byteOffset(), u.byteLength())));
    }

    @Test
    public void testParseDoubleBitsCharArrayIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> testBits(t, u -> FastDoubleParser.parseDoubleBits(u.input().toCharArray(), u.charOffset(), u.charLength())));
    }

    @Test
    public void testParseDoubleBitsCharSequenceIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> testBits(t, u -> FastDoubleParser.parseDoubleBits(u.input(), u.charOffset(), u.charLength())));
    }

    @Test
    public void testParseDoubleBitsMemIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> testBits(t, u -> {
                                    final String s = u.input();
                                    final int len = s.length();
                                    long mem = Unsafe.malloc(len, MemoryTag.NATIVE_DEFAULT);
                                    try {
                                        // the function under the test cannot validate length of
                                        // the memory pointer. This validation has to be done externally.
                                        if (s.length() < u.charOffset() + u.charLength()) {
                                            throw NumericException.INSTANCE;
                                        }
                                        Chars.asciiStrCpy(s, len, mem);
                                        return FastDoubleParser.parseDouble(mem, u.charOffset(), u.charLength());
                                    } finally {
                                        Unsafe.free(mem, len, MemoryTag.NATIVE_DEFAULT);
                                    }
                                }
                        )
                );
    }

    @Test
    public void testParseDoubleByteArray() {
        createAllTestDataForDouble().stream()
                .filter(t -> t.charLength() == t.input().length()
                        && t.charOffset() == 0)
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input().getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testParseDoubleByteArrayIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input().getBytes(StandardCharsets.UTF_8), u.byteOffset(), u.byteLength())));
    }

    @Test
    public void testParseDoubleCharArray() {
        createAllTestDataForDouble().stream()
                .filter(t -> t.charLength() == t.input().length()
                        && t.charOffset() == 0)
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input().toCharArray())));
    }

    @Test
    public void testParseDoubleCharArrayIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input().toCharArray(), u.charOffset(), u.charLength())));
    }

    @Test
    public void testParseDoubleCharSequence() {
        createAllTestDataForDouble().stream()
                .filter(t -> t.charLength() == t.input().length()
                        && t.charOffset() == 0)
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input())));
    }

    @Test
    public void testParseDoubleCharSequenceIntInt() {
        createAllTestDataForDouble()
                .forEach(t -> test(t, u -> FastDoubleParser.parseDouble(u.input(), u.charOffset(), u.charLength())));
    }

    private void test(TestData d, ToDoubleFunction<TestData> f) {
        if (!d.valid()) {
            try {
                f.applyAsDouble(d);
                fail();
            } catch (Exception e) {
                //success
            }
        } else {
            try {
                assertEquals(d.expectedDoubleValue(), f.applyAsDouble(d), 0.001);
            } catch (NumericException e) {
                throw new NumberFormatException();
            }
        }
    }

    private void testBits(TestData d, ToDoubleFunction<TestData> f) {
        if (!d.valid()) {
            try {
                f.applyAsDouble(d);
                fail();
            } catch (NumericException e) {
                // good
            }
        } else {
            try {
                assertEquals(d.expectedDoubleValue(), f.applyAsDouble(d), 0.001);
            } catch (NumericException e) {
                throw new NumberFormatException();
            }
        }
    }

    @FunctionalInterface
    public interface ToDoubleFunction<T> {
        double applyAsDouble(T value) throws NumericException;
    }
}
