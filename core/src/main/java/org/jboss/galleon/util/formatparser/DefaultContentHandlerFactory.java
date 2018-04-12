/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

import org.jboss.galleon.util.formatparser.formats.CollectionParsingFormat;
import org.jboss.galleon.util.formatparser.formats.KeyValueParsingFormat;
import org.jboss.galleon.util.formatparser.formats.MapParsingFormat;
import org.jboss.galleon.util.formatparser.formats.StringParsingFormat;
import org.jboss.galleon.util.formatparser.formats.WildcardParsingFormat;
import org.jboss.galleon.util.formatparser.handlers.KeyValueContentHandler;
import org.jboss.galleon.util.formatparser.handlers.ListContentHandler;
import org.jboss.galleon.util.formatparser.handlers.MapContentHandler;
import org.jboss.galleon.util.formatparser.handlers.SetContentHandler;
import org.jboss.galleon.util.formatparser.handlers.StringContentHandler;
import org.jboss.galleon.util.formatparser.handlers.WildcardContentHandler;

/**
 *
 * @author Alexey Loubyansky
 */
public class DefaultContentHandlerFactory implements FormatContentHandlerFactory {

    private static final DefaultContentHandlerFactory INSTANCE = new DefaultContentHandlerFactory();

    public static DefaultContentHandlerFactory getInstance() {
        return INSTANCE;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingCallbackHandlerFactory#forFormat(org.jboss.galleon.spec.type.ParsingFormat)
     */
    @Override
    public FormatContentHandler forFormat(ParsingFormat format, int strIndex) throws FormatParsingException {
        final String name = format.getContentType();
        if(name.equals(StringParsingFormat.NAME)) {
            return new StringContentHandler(format, strIndex);
        } else if(name.equals(CollectionParsingFormat.LIST)) {
            return new ListContentHandler(format, strIndex);
        } else if(name.equals(KeyValueParsingFormat.NAME)) {
            return new KeyValueContentHandler(format, strIndex);
        } else if(name.equals(MapParsingFormat.NAME)) {
            return new MapContentHandler(format, strIndex);
        } else if(name.equals(CollectionParsingFormat.SET)) {
            return new SetContentHandler(format, strIndex);
        } else if(name.equals(WildcardParsingFormat.NAME)) {
            return new WildcardContentHandler(format, strIndex);
        } else {
            throw new FormatParsingException("Unexpected content type " + format);
        }
    }
}
