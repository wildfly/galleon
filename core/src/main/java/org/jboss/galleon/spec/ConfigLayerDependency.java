/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayerDependency implements Comparable<ConfigLayerDependency> {


    /**
     * Creates a required dependency on a layer.
     *
     * @param name  layer name
     * @return  dependency spec
     */
    public static ConfigLayerDependency forLayer(String name) {
        return new ConfigLayerDependency(name, false);
    }

    /**
     * Creates a dependency on a layer.
     *
     * @param name  layer name
     * @param optional  whether the dependency is optional
     * @return  dependency spec
     */
    public static ConfigLayerDependency forLayer(String name, boolean optional) {
        return new ConfigLayerDependency(name, optional);
    }

    private final String name;
    private final boolean optional;

    protected ConfigLayerDependency(String name, boolean optional) {
        this.name = name;
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (optional ? 1231 : 1237);
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
        ConfigLayerDependency other = (ConfigLayerDependency) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (optional != other.optional)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[')
            .append(name)
            .append(optional ? " optional]" : " required");
        return buf.append(']').toString();
    }

    @Override
    public int compareTo(ConfigLayerDependency o) {
        return name.compareTo(o.name);
    }
}
