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

package org.jboss.galleon.universe;

/**
 *
 * @author Alexey Loubyansky
 */
public class UniverseSpec {

    public static UniverseSpec fromString(String src) {
        final int i = src.indexOf('/');
        return i < 0 ? new UniverseSpec(src, null) : new UniverseSpec(src.substring(0, i), src.substring(i + 1));
    }

    private final String factory;
    private final String location;
    private final int hash;

    public UniverseSpec(String factory, String location) {
        this.factory = factory;
        this.location = location;

        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((factory == null ) ? 0 : factory.hashCode());
        hash = prime * hash + ((location == null) ? 0 : location.hashCode());
        this.hash = hash;
    }

    public String getFactory() {
        return factory;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UniverseSpec other = (UniverseSpec) obj;
        if (factory == null) {
            if (other.factory != null)
                return false;
        } else if (!factory.equals(other.factory))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        return true;
    }

    public String toString() {
        return location == null ? factory : factory + '/' + location;
    }
}
