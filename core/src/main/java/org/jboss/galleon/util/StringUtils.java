/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.util;

import java.util.Iterator;
import java.util.List;

/**
 * @author Alexey Loubyansky
 *
 */
public class StringUtils {

    public static void append(StringBuilder buf, Iterable<?> i) {
        final Iterator<?> it = i.iterator();
        if(!it.hasNext()) {
            buf.append("[]");
            return;
        }
        buf.append(it.next());
        while(it.hasNext()) {
            buf.append(',').append(it.next());
        }
    }

    public static void appendList(StringBuilder buf, List<?> list) {
        if(list.isEmpty()) {
            buf.append("[]");
            return;
        }
        buf.append(list.get(0));
        for(int i = 1; i < list.size(); ++i) {
            buf.append(',').append(list.get(i));
        }
    }

    /**
     * Strings the surrounding character from the string.
     * <p>
     * If the string both starts with and ends with the character the value will be returned without the surrounding
     * character. If the value does not both start and end with the character the value will be returned as-is.
     * </p>
     *
     * @param value the value to strip the start and end character from if they exist
     * @param c     the character to remove from the start and end of the value
     *
     * @return the value with the surrounding character removed if the value starts and ends with the character,
     * otherwise the value is returned as-is.
     */
    public static String stripSurrounding(final String value, final char c) {
        if (value.charAt(0) == c) {
            final int len = value.length();
            final int last = len - 1;
            if (value.charAt(last) == c) {
                return value.substring(1, last);
            }
        }
        return value;
    }
}
