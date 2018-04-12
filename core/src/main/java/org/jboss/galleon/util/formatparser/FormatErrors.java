/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.util.formatparser;

/**
 * @author Alexey Loubyansky
 *
 */
public class FormatErrors {

    public static String formatExprDoesNotSupportTypeParam(String name) {
        return "Format " + name + " does not support type parameters";
    }

    public static String parsingFailed(String str, int errorIndex, ParsingFormat format, int formatStartIndex) {
        final StringBuilder buf = new StringBuilder().append("Parsing of '").append(str).append("' failed");
        if(str.length() != errorIndex) {
            buf.append(" at index ").append(errorIndex);
        }
        return buf.append(" while parsing format ").append(format).append(" started on index ").append(formatStartIndex).toString();
    }

    public static String formatEndedPrematurely(ParsingFormat format) {
        return new StringBuilder()
                .append("Format ").append(format).append(" has ended prematurely")
                .toString();
    }

    public static String formatIncomplete(ParsingFormat format) {
        return new StringBuilder()
                .append("Format ").append(format).append(" is incomplete")
                .toString();
    }

    public static String unexpectedStartingCharacter(ParsingFormat format, char expected, char actual) {
        return new StringBuilder()
                .append("Format ").append(format).append(" expects '").append(expected).append("' as it's starting character, not '").append(actual).append("'")
                .toString();
    }

    public static String unexpectedChildFormat(ParsingFormat parent, ParsingFormat child) {
        return "Format " + parent + " does not expect format " + child + " as a child.";
    }

    public static String unexpectedCompositeFormatElement(ParsingFormat format, Object elem) {
        if(elem == null) {
            return "Unexpected attribute for " + format;
        }
        return "Format " + format + " does not accept attribute " + elem;
    }
}
