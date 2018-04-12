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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.formatparser.FormatErrors;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingContext;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class CompositeParsingFormat extends ObjectParsingFormat {

    public static CompositeParsingFormat newInstance() {
        return newInstance(null);
    }

    public static CompositeParsingFormat newInstance(String name) {
        return new CompositeParsingFormat(name == null ? ObjectParsingFormat.NAME : name);
    }

    public static CompositeParsingFormat newInstance(String name, String contentType) {
        return new CompositeParsingFormat(name == null ? ObjectParsingFormat.NAME : name, contentType);
    }

    public static CompositeParsingFormat newInstance(String name, KeyValueParsingFormat entryFormat) {
        return new CompositeParsingFormat(name == null ? ObjectParsingFormat.NAME : name, entryFormat);
    }

    public static CompositeParsingFormat newInstance(String name, String contentType, KeyValueParsingFormat entryFormat) {
        return new CompositeParsingFormat(name == null ? ObjectParsingFormat.NAME : name, contentType, entryFormat);
    }

    private boolean acceptAll = false;
    private Map<String, ParsingFormat> elems = Collections.emptyMap();

    protected CompositeParsingFormat(String name) {
        super(name);
    }

    protected CompositeParsingFormat(String name, String contentType) {
        super(name, contentType);
    }

    protected CompositeParsingFormat(String name, KeyValueParsingFormat entryFormat) {
        super(name, entryFormat);
    }

    protected CompositeParsingFormat(String name, String contentType, KeyValueParsingFormat entryFormat) {
        super(name, contentType, entryFormat);
    }

    public CompositeParsingFormat setAcceptAll(boolean acceptAll) {
        this.acceptAll = acceptAll;
        return this;
    }

    public CompositeParsingFormat addElement(String name) {
        return addElement(name, entryFormat);
    }

    public CompositeParsingFormat addElement(String name, ParsingFormat valueFormat) {
        elems = CollectionUtils.put(elems, name, valueFormat == null ? entryFormat
                : KeyValueParsingFormat.newInstance(entryFormat.getKeyFormat(), entryFormat.getSeparator(), valueFormat));
        return this;
    }

    @Override
    public boolean isAcceptsKey(Object name) {
        return acceptAll || elems.containsKey(name);
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }

        Map.Entry<String, ParsingFormat> matchedElem = null;
        for(Map.Entry<String, ParsingFormat> elem : elems.entrySet()) {
            if(ctx.startsNow(elem.getKey())) {
                if(matchedElem == null) {
                    matchedElem = elem;
                } else if(matchedElem.getKey().length() < elem.getKey().length()) {
                    matchedElem = elem;
                }
            }
        }

        ParsingFormat valueFormat = null;
        if(matchedElem == null) {
            if(!acceptAll) {
                throw new FormatParsingException(FormatErrors.unexpectedCompositeFormatElement(this, null));
            }
            valueFormat = entryFormat;
        } else {
            valueFormat = matchedElem.getValue();
        }

        ctx.pushFormat(valueFormat);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (acceptAll ? 1231 : 1237);
        result = prime * result + ((elems == null) ? 0 : elems.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeParsingFormat other = (CompositeParsingFormat) obj;
        if (acceptAll != other.acceptAll)
            return false;
        if (elems == null) {
            if (other.elems != null)
                return false;
        } else if (!elems.equals(other.elems))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        if(name != null) {
            buf.append("!name:").append(name);
        }
        if(contentType != null) {
            buf.append("!content-type:").append(contentType);
        }
        if (!elems.isEmpty()) {
            final Iterator<Map.Entry<String, ParsingFormat>> i = elems.entrySet().iterator();
            Map.Entry<String, ParsingFormat> elem = i.next();
            buf.append(elem.getKey()).append(':').append(((KeyValueParsingFormat)elem.getValue()).getValueFormat());
            while (i.hasNext()) {
                elem = i.next();
                buf.append(',').append(elem.getKey()).append(':').append(((KeyValueParsingFormat)elem.getValue()).getValueFormat());

            }
        }
        return buf.append('}').toString();
    }
}
