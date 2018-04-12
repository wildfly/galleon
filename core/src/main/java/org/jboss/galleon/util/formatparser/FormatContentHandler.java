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
 *
 * @author Alexey Loubyansky
 */
public abstract class FormatContentHandler {

    protected final ParsingFormat format;
    protected final int strIndex;

    public FormatContentHandler(ParsingFormat format, int strIndex) {
        this.format = format;
        this.strIndex = strIndex;
    }

    public ParsingFormat getFormat() {
        return format;
    }

    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        throw new FormatParsingException("Format " + format + " does not support nested formats");
    }

    public void character(char ch) throws FormatParsingException {
        throw new FormatParsingException("Format " + format + " does not support character content");
    }

    public abstract Object getContent() throws FormatParsingException;
}
