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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureAnnotation {

    // BUILT-IN ANNOTATIONS
    public static final String FEATURE_BRANCH = "feature-branch";

    public static final String FEATURE_BRANCH_PARENT_CHILDREN = "parent-children";
    public static final String FEATURE_BRANCH_SPEC = "spec";

    public static final String FEATURE_BRANCH_BATCH = "batch";

    public static FeatureAnnotation parentChildrenBranch() {
        return new FeatureAnnotation(FEATURE_BRANCH).setElement(FEATURE_BRANCH_PARENT_CHILDREN);
    }

    public static FeatureAnnotation parentChildrenBranch(boolean batch) {
        return parentChildrenBranch().setElement(FEATURE_BRANCH_BATCH, String.valueOf(batch));
    }

    public static FeatureAnnotation specBranch(boolean spec) {
        return new FeatureAnnotation(FEATURE_BRANCH).setElement(FEATURE_BRANCH_SPEC, String.valueOf(spec));
    }

    public static FeatureAnnotation specBranch(boolean spec, boolean batch) {
        return specBranch(spec).setElement(FEATURE_BRANCH_BATCH, String.valueOf(batch));
    }

    final String name;
    private Map<String, String> elems = Collections.emptyMap();

    public FeatureAnnotation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public FeatureAnnotation setElement(String name) {
        return setElement(name, null);
    }

    public FeatureAnnotation setElement(String name, String value) {
        elems = CollectionUtils.put(elems, name, value);
        return this;
    }

    public boolean hasElements() {
        return !elems.isEmpty();
    }

    public Map<String, String> getElements() {
        return elems;
    }

    public boolean hasElement(String name) {
        return elems.containsKey(name);
    }

    public String getElement(String name) {
        return elems.get(name);
    }

    public String getElement(String name, String defaultValue) {
        return elems.getOrDefault(name, defaultValue);
    }

    public String getElement(String elem, boolean required) throws ProvisioningDescriptionException {
        final String value = elems.get(elem);
        if(value == null) {
            if(required) {
                throw new ProvisioningDescriptionException("Annotation " + name + " is missing required element " + elem);
            }
            return null;
        }
        return value;
    }

    public List<String> getElementAsList(String name) {
        return parseList(elems.get(name));
    }

    public List<String> getElementAsList(String name, String defaultValue) {
        return parseList(elems.getOrDefault(name, defaultValue));
    }

    private List<String> parseList(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptyList();
        }
        int comma = str.indexOf(',');
        if (comma < 1) {
            return Collections.singletonList(str.trim());
        }
        final List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, ",", false);
        while (tokenizer.hasMoreTokens()) {
            final String paramName = tokenizer.nextToken().trim();
            if (paramName.isEmpty()) {
                continue;
            }
            list.add(paramName);
        }
        return list;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elems == null) ? 0 : elems.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        FeatureAnnotation other = (FeatureAnnotation) obj;
        if (elems == null) {
            if (other.elems != null)
                return false;
        } else if (!elems.equals(other.elems))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("name=").append(name);
        if(!elems.isEmpty()) {
            buf.append(" elems={");
            final Iterator<Map.Entry<String, String>> i = elems.entrySet().iterator();
            Map.Entry<String, String> entry = i.next();
            buf.append(entry.getKey());
            if(entry.getValue() != null) {
                buf.append('=').append(entry.getValue());
            }
            while(i.hasNext()) {
                entry = i.next();
                buf.append(';').append(entry.getKey());
                if(entry.getValue() != null) {
                    buf.append('=').append(entry.getValue());
                }
            }
            buf.append('}');
        }
        return buf.toString();
    }
}
