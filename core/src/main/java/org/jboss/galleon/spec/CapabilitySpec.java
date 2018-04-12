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
package org.jboss.galleon.spec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.CapabilityResolver;
import org.jboss.galleon.util.CollectionUtils;

/**
 * @author Alexey Loubyansky
 *
 */
public class CapabilitySpec {

    public static CapabilitySpec fromString(String str) throws ProvisioningDescriptionException {
        return fromString(str, false);
    }

    public static CapabilitySpec fromString(String str, boolean optional) throws ProvisioningDescriptionException {
        if(str == null) {
            throw new ProvisioningDescriptionException("str is null");
        }
        if(str.isEmpty()) {
            throw new ProvisioningDescriptionException("str is empty");
        }

        List<String> elems = Collections.emptyList();
        List<Boolean> isElemStatic = Collections.emptyList();
        int strI = 0;
        final StringBuilder buf = new StringBuilder();
        boolean isStatic = true;
        while(strI < str.length()) {
            final char ch = str.charAt(strI++);
            switch(ch) {
                case '.': {
                    if(buf.length() == 0) {
                        formatError(str);
                    }
                    if(isStatic) {
                        if(buf.charAt(buf.length() - 1) == '.') {
                            formatError(str);
                        }
                        if(strI < str.length() && str.charAt(strI) != '$') {
                            buf.append('.');
                            break;
                        }
                    }
                    elems = CollectionUtils.add(elems, buf.toString());
                    isElemStatic = CollectionUtils.add(isElemStatic, isStatic);
                    buf.setLength(0);
                    isStatic = true;
                    break;
                }
                case '$': {
                    if(strI > 1 && str.charAt(strI - 2) != '.') {
                        formatError(str);
                    }
                    isStatic = false;
                    break;
                } default: {
                    if(Character.isWhitespace(ch)) {
                        throw new ProvisioningDescriptionException("Whitespaces are not allowed in a capability expression '" + str + "'");
                    }
                    buf.append(ch);
                }
            }
        }
        if(buf.length() == 0) {
            formatError(str);
        }
        elems = CollectionUtils.add(elems, buf.toString());
        isElemStatic = CollectionUtils.add(isElemStatic, isStatic);
        return new CapabilitySpec(elems, isElemStatic, optional);
    }

    private static void formatError(String str) throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException("Capability '" + str + "' doesn't follow format [$]part[.[$]part]");
    }

    private final String[] elems;
    private final Boolean[] isElemStatic;
    private final boolean optional;

    private CapabilitySpec(List<String> elems, List<Boolean> elemTypes, boolean optional) throws ProvisioningDescriptionException {
        this.elems = elems.toArray(new String[elems.size()]);
        this.isElemStatic = elemTypes.toArray(new Boolean[elemTypes.size()]);
        this.optional = optional;
        if(optional && isStatic()) {
            throw new ProvisioningDescriptionException("Static capability cannot be optional: " + toString());
        }
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isStatic() {
        return elems.length == 1 && isElemStatic[0];
    }

    public boolean resolve(CapabilityResolver resolver) throws ProvisioningException {
        for(int i = 0; i < elems.length; ++i) {
            if(!resolver.resolveElement(elems[i], isElemStatic[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (optional ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(isElemStatic);
        result = prime * result + Arrays.hashCode(elems);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CapabilitySpec other = (CapabilitySpec) obj;
        if (optional != other.optional)
            return false;
        if (!Arrays.equals(isElemStatic, other.isElemStatic))
            return false;
        if (!Arrays.equals(elems, other.elems))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if(elems.length == 1 && isElemStatic[0]) {
            return elems[0];
        }
        final StringBuilder buf = new StringBuilder();
        if(!isElemStatic[0] ) {
            buf.append('$');
        }
        buf.append(elems[0]);
        for(int i = 1; i < elems.length; ++i) {
            buf.append('.');
            if(!isElemStatic[i]) {
                buf.append('$');
            }
            buf.append(elems[i]);
        }
        return buf.toString();
    }
}
