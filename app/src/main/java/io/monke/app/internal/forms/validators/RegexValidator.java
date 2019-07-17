/*
 * Copyright (C) by MinterTeam. 2018
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.monke.app.internal.forms.validators;

import java.util.regex.Pattern;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class RegexValidator extends BaseValidator {
    private final String mPattern;

    public RegexValidator(String pattern) {
        super("Field is invalid");
        mPattern = pattern;
    }

    public RegexValidator(String pattern, boolean required) {
        super("Field is invalid", required);
        mPattern = pattern;
    }

    public RegexValidator(Pattern pattern, CharSequence errorMessage) {
        super(errorMessage);
        mPattern = pattern.pattern();
    }

    public RegexValidator(Pattern pattern, CharSequence errorMessage, boolean required) {
        super(errorMessage, required);
        mPattern = pattern.pattern();
    }

    public RegexValidator(String pattern, CharSequence errorMessage) {
        super(errorMessage);
        mPattern = pattern;
    }

    public RegexValidator(String pattern, CharSequence errorMessage, boolean required) {
        super(errorMessage, required);
        mPattern = pattern;
    }

    @Override
    protected boolean getCondition(CharSequence value) {
        if (!isRequired() && (value == null || value.length() == 0)) {
            return true;
        }

        String val = value == null ? "" : value.toString();
        return val.matches(mPattern);
    }
}
