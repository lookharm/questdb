/*
 * @(#)HandPickedTest.java
 * Copyright © 2021. Werner Randelshofer, Switzerland. MIT License.
 */

package io.questdb.std.fastdouble;

import io.questdb.std.NumericException;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

abstract class AbstractFloatHandPickedTest {

    /**
     * Tests input classes that execute different code branches in
     * method {@link FastFloatMath#tryDecToFloatWithFastAlgorithm(boolean, long, int)}.
     */
    @Test
    public void testDecFloatLiteralClingerInputClasses() {
        //
        testLegalInput("Inside Clinger fast path (max_clinger_significand, max_clinger_exponent)", "16777215e10");
        testLegalInput("Outside Clinger fast path (max_clinger_significand, max_clinger_exponent + 1)", "16777215e11");
        testLegalInput("Outside Clinger fast path (max_clinger_significand + 1, max_clinger_exponent)", "16777216e10");
        testLegalInput("Inside Clinger fast path (min_clinger_significand + 1, min_clinger_exponent)", "1e-10");
        testLegalInput("Outside Clinger fast path (min_clinger_significand + 1, min_clinger_exponent - 1)", "1e-11");
    }

    /**
     * Tests input classes that execute different code branches in
     * method {@link FastDoubleMath#tryHexFloatToDouble(boolean, long, int)}.
     */
    @Test
    public void testHexFloatLiteralClingerInputClasses() {
        testLegalInput("Inside Clinger fast path (max_clinger_significand)", "0x1fffffffffffffp74", 0x1fffffffffffffp74f);
        testLegalInput("Outside Clinger fast path (max_clinger_significand, max_clinger_exponent + 1)", "0x1fffffffffffffp74", 0x1fffffffffffffp74f);
        testLegalInput("Outside Clinger fast path (max_clinger_significand + 1, max_clinger_exponent)", "0x20000000000000p74", 0x20000000000000p74f);
        testLegalInput("Inside Clinger fast path (min_clinger_significand + 1, min_clinger_exponent)", "0x1p-74", 0x1p-74f);
        testLegalInput("Outside Clinger fast path (min_clinger_significand + 1, min_clinger_exponent - 1)", "0x1p-75", 0x1p-75f);
    }

    @Test
    public void testIllegalInputs() {
        testIllegalInput("0." + (char) 0x3231 + (char) 0x0000 + "345678");
        testIllegalInput("");
        testIllegalInput("-");
        testIllegalInput("+");
        testIllegalInput("1e");
        testIllegalInput("1ee2");
        testIllegalInput("1_000");
        testIllegalInput("0.000_1");
        testIllegalInput("-e-55");
        testIllegalInput("1 x");
        testIllegalInput("x 1");
        testIllegalInput("1§");
        testIllegalInput("NaN x");
        testIllegalInput("Infinity x");
        testIllegalInput("0x123.456789abcde");
        testIllegalInput(".");
        testIllegalInput("0x.");
        testIllegalInput(".e2");
    }

    @Test
    public void testIllegalInputsWithPrefixAndSuffix() {
        testIllegalInputWithPrefixAndSuffix("before-after", 6, 1);
        testIllegalInputWithPrefixAndSuffix("before7.78$after", 6, 5);
        testIllegalInputWithPrefixAndSuffix("before7.78e$after", 6, 6);
        testIllegalInputWithPrefixAndSuffix("before0x123$4after", 6, 7);
        testIllegalInputWithPrefixAndSuffix("before0x123.4$after", 6, 8);
        testIllegalInputWithPrefixAndSuffix("before0$123.4after", 6, 7);
    }

    @Test
    public void testLegalDecFloatLiterals() {
        testLegalInput("-0.0", -0.0f);
        testLegalInput("0.12345678", 0.12345678f);
        testLegalInput("1e23", 1e23f);
        testLegalInput("whitespace before 1", " 1");
        testLegalInput("whitespace after 1", "1 ");
        testLegalInput("0", 0.0f);
        testLegalInput("-0", -0.0f);
        testLegalInput("+0", +0.0f);
        testLegalInput("-0.0", -0.0f);
        testLegalInput("-0.0e-22", -0.0e-22f);
        testLegalInput("-0.0e24", -0.0e24f);
        testLegalInput("0e555", 0.0f);
        testLegalInput("-0e555", -0.0f);
        testLegalInput("1", 1.0f);
        testLegalInput("-1", -1.0f);
        testLegalInput("+1", +1.0f);
        testLegalInput("1e0", 1e0f);
        testLegalInput("1.e0", 1e0f);
        testLegalInput("1e1", 1e1f);
        testLegalInput("1e+1", 1e+1f);
        testLegalInput("1e-1", 1e-1f);
        testLegalInput("0049", 49f);
        testLegalInput("9999999999999999999", 9999999999999999999f);
        testLegalInput("972150611626518208.0", 9.7215061162651827E17f);
        testLegalInput("0.1e+3", 100.0f);
        testLegalInput("0.00000000000000000000000000000000000000000001e+46", 100.0f);
        testLegalInput("10000000000000000000000000000000000000000000e+308", Float.parseFloat("10000000000000000000000000000000000000000000e+308"));
        testLegalInput("3.1415926535897932384626433832795028841971693993751", Float.parseFloat("3.1415926535897932384626433832795028841971693993751"));
        testLegalInput("314159265358979323846.26433832795028841971693993751e-20", 3.141592653589793f);
        testLegalInput("1e-326", 0.0f);
        testLegalInput("1e-325", 0.0f);
        testLegalInput("1e310", Float.POSITIVE_INFINITY);
        testLegalDecInput(7.2057594037927933e+16f);
        testLegalDecInput(-7.2057594037927933e+16f);
    }

    @Test
    public void testLegalDecFloatLiteralsExtremeValues() {
        testLegalDecInput(Float.MIN_VALUE);
        testLegalDecInput(Float.MAX_VALUE);
        testLegalDecInput(Float.POSITIVE_INFINITY);
        testLegalDecInput(Float.NEGATIVE_INFINITY);
        testLegalDecInput(Float.NaN);
        testLegalDecInput(Math.nextUp(0.0f));
        testLegalDecInput(Math.nextDown(0.0f));
        testLegalInput("Just above MAX_VALUE: 3.4028236e+38f", "3.4028236e+38", Float.POSITIVE_INFINITY);
        testLegalInput("Just below MIN_VALUE:  1.3e-45f", "0.7e-45", 0.0f);
    }

    @Test
    public void testLegalHexFloatLiterals() {
        testLegalInput("0x0.1234ab78p0", 0x0.1234ab78p0f);
        testLegalInput("0x0.1234AB78p0", 0x0.1234AB78p0f);
        testLegalInput("0x1.0p8", 256f);
    }

    @Test
    public void testLegalHexFloatLiteralsExtremeValues() {
        testLegalHexInput(Float.MIN_VALUE);
        testLegalHexInput(Float.MAX_VALUE);
        testLegalHexInput(Float.POSITIVE_INFINITY);
        testLegalHexInput(Float.NEGATIVE_INFINITY);
        testLegalHexInput(Float.NaN);
        testLegalHexInput(Math.nextUp(0.0f));
        testLegalHexInput(Math.nextDown(0.0f));
        testLegalInput("Just above MAX_VALUE: 0x1.fffffffffffff8p1023", "0x1.fffffffffffff8p1023", Float.POSITIVE_INFINITY);
        testLegalInput("Just below MIN_VALUE: 0x0.00000000000008p-1022", "0x0.00000000000008p-1022", 0.0f);
    }

    @Test
    public void testLegalInputsWithPrefixAndSuffix() throws NumericException {
        testLegalInputWithPrefixAndSuffix("before-1after", 6, 2, -1.0f);
        testLegalInputWithPrefixAndSuffix("before7.789after", 6, 5, 7.789f);
        testLegalInputWithPrefixAndSuffix("before7.78e2after", 6, 6, 7.78e2f);
        testLegalInputWithPrefixAndSuffix("before0x1234p0after", 6, 8, 0x1234p0f);
        testLegalInputWithPrefixAndSuffix("before0x123.45p0after", 6, 10, 0x123.45p0f);
        testLegalInputWithPrefixAndSuffix("Outside Clinger fast path (min_clinger_significand + 1, min_clinger_exponent - 1)", "before1e-23after", 6, 5, 1e-23f);
    }

    @Test
    public void testPowerOfTen() {
        IntStream.range(-307, 309).mapToObj(i -> "1e" + i)
                .forEach(d -> testLegalInput(d, Float.parseFloat(d)));
    }

    abstract float parse(CharSequence str) throws NumericException;

    protected abstract float parse(String str, int offset, int length) throws NumericException;

    private void testIllegalInput(String s) {
        try {
            parse(s);
            fail();
        } catch (NumericException e) {
            // success
        }
    }

    private void testIllegalInputWithPrefixAndSuffix(String str, int offset, int length) {
        assertThrows(NumericException.class, () -> parse(str, offset, length));
    }

    private void testLegalDecInput(float expected) {
        testLegalDecInput(expected + "", expected);
    }

    private void testLegalDecInput(String testName, float expected) {
        testLegalInput(testName, expected + "", expected);
    }

    private void testLegalHexInput(float expected) {
        testLegalHexInput(Float.toHexString(expected), expected);
    }

    private void testLegalHexInput(String testName, float expected) {
        testLegalInput(testName, Float.toHexString(expected), expected);
    }

    private void testLegalInput(String testName, String str) {
        testLegalInput(testName, str, Float.parseFloat(str));
    }

    private void testLegalInput(String str, float expected) {
        testLegalInput(str, str, expected);
    }

    private void testLegalInput(String testName, String str, float expected) {
        float actual;
        try {
            actual = parse(str);
        } catch (NumericException e) {
            throw new NumberFormatException();
        }
        Assert.assertEquals(testName, expected, actual, 0.001);
        Assert.assertEquals("intBits of " + expected, Float.floatToIntBits(expected), Float.floatToIntBits(actual));
    }

    private void testLegalInputWithPrefixAndSuffix(String str, int offset, int length, float expected) throws NumericException {
        testLegalInputWithPrefixAndSuffix(str, str, offset, length, expected);
    }

    private void testLegalInputWithPrefixAndSuffix(String testName, String str, int offset, int length, float expected) throws NumericException {
        float actual = parse(str, offset, length);
        Assert.assertEquals(testName, expected, actual, 0.001);
    }
}