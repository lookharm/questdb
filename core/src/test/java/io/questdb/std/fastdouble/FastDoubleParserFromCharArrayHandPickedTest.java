/*
 * @(#)FastDoubleParserHandPickedTest.java
 * Copyright © 2021. Werner Randelshofer, Switzerland. MIT License.
 */

package io.questdb.std.fastdouble;

import io.questdb.std.NumericException;

public class FastDoubleParserFromCharArrayHandPickedTest extends AbstractDoubleHandPickedTest {
    @Override
    double parse(CharSequence str) throws NumericException {
        char[] chars = new char[str.length()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = str.charAt(i);
        }
        return FastDoubleParser.parseDouble(chars);
    }

    @Override
    protected double parse(String str, int offset, int length) throws NumericException {
        return FastDoubleParser.parseDouble(str.toCharArray(), offset, length);
    }
}
