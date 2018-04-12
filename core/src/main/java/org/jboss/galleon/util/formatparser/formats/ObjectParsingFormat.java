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
package org.jboss.galleon.util.formatparser.formats;

/**
 *
 * @author Alexey Loubyansky
 */
public class ObjectParsingFormat extends MapParsingFormat {

    public static final String NAME = "Object";

    public static ObjectParsingFormat getInstance() {
        return new ObjectParsingFormat();
    }

    protected ObjectParsingFormat() {
        this(NAME);
    }

    protected ObjectParsingFormat(String name) {
        super(name);
        setEntryFormat(KeyValueParsingFormat.newInstance(StringParsingFormat.getInstance(), KeyValueParsingFormat.SEPARATOR, WildcardParsingFormat.getInstance(this)));
    }

    protected ObjectParsingFormat(String name, String contentType) {
        super(name, contentType);
    }

    protected ObjectParsingFormat(String name, KeyValueParsingFormat entryFormat) {
        super(name, entryFormat);
    }

    protected ObjectParsingFormat(String name, String contentType, KeyValueParsingFormat entryFormat) {
        super(name, contentType, entryFormat);
    }

    @Override
    public String toString() {
        return NAME;
    }
}
