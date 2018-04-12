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
package org.jboss.galleon.util.formatparser.formats.expr;

import java.util.Collections;
import java.util.List;

import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;
import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.jboss.galleon.util.formatparser.formats.KeyValueParsingFormat;
import org.jboss.galleon.util.formatparser.formats.MapParsingFormat;
import org.jboss.galleon.util.formatparser.formats.ObjectParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.jboss.galleon.util.formatparser.formats.WildcardParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatExprContentHandler extends FormatContentHandler {

        private final StringBuilder buf = new StringBuilder();
        private List<ParsingFormat> params = Collections.emptyList();

        public FormatExprContentHandler(ParsingFormat format, int strIndex) {
            super(format, strIndex);
        }

        @Override
        public void character(char ch) throws FormatParsingException {
            buf.append(ch);
        }

        @SuppressWarnings("unchecked")
        public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
            if(!params.isEmpty()) {
                throw new FormatParsingException("Format " + buf + " type parameters have already been initialized");
            }
            params = (List<ParsingFormat>) childHandler.getContent();
        }

        @Override
        public Object getContent() throws FormatParsingException {
            return resolveFormat();
        }

        public ParsingFormat resolveFormat() throws FormatParsingException {
            final String name = buf == null ? null : buf.toString();

            if(name == null || name.length() == 0) {
                if(params.size() != 1) {
                    throw new FormatParsingException("Format type was not specified");
                }
                return params.get(0);
            }

            if(StringParsingFormat.NAME.equals(name)) {
                assertNoTypeParam(name);
                return StringParsingFormat.getInstance();
            }

            if(CollectionParsingFormat.LIST.equals(name)) {
                final ParsingFormat item = assertSingleTypeParam(name);
                if(item == null) {
                    return CollectionParsingFormat.list();
                }
                return CollectionParsingFormat.list(item);
            }

            if(CollectionParsingFormat.SET.equals(name)) {
                final ParsingFormat item = assertSingleTypeParam(name);
                if(item == null) {
                    return CollectionParsingFormat.set();
                }
                return CollectionParsingFormat.set(item);
            }

            if(WildcardParsingFormat.NAME.equals(name)) {
                assertNoTypeParam(name);
                return WildcardParsingFormat.getInstance();
            }

            if(ObjectParsingFormat.NAME.equals(name)) {
                assertNoTypeParam(name);
                return ObjectParsingFormat.getInstance();
            }

            if(MapParsingFormat.NAME.equals(name)) {
                if(params.isEmpty()) {
                    return MapParsingFormat.getInstance();
                }
                if(params.size() != 2) {
                    throw new FormatParsingException("Map expects two parameter types but got " + params);
                }
                return MapParsingFormat.getInstance(KeyValueParsingFormat.newInstance(params.get(0), params.get(1)));
            }

            throw new FormatParsingException("Unexpected format name " + name);
        }

        private ParsingFormat assertSingleTypeParam(String name) throws FormatParsingException {
            if(params.isEmpty()) {
                return null;
            }
            if(params.size() == 1) {
                return params.get(0);
            }
            throw new FormatParsingException("Format " + name + " accepts only one type parameter");
        }

        private void assertNoTypeParam(final String name) throws FormatParsingException {
            if(!params.isEmpty()) {
                throw new FormatParsingException(FormatErrors.formatExprDoesNotSupportTypeParam(name));
            }
        }
    }