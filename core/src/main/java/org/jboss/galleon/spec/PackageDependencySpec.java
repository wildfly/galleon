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

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;

/**
 * Describes dependency on a single package.
 *
 * @author Alexey Loubyansky
 */
public class PackageDependencySpec implements Comparable<PackageDependencySpec> {

    public static final int OPTIONAL    = 0b001;
    public static final int PASSIVE     = 0b011;
    public static final int REQUIRED    = 0b100;

    public static boolean isOptional(int type) {
        return type != REQUIRED;
    }

    /**
     * Creates a required dependency on the package.
     *
     * @param name  target package name
     * @return  dependency spec
     */
    public static PackageDependencySpec required(String name) {
        return new PackageDependencySpec(name, REQUIRED);
    }

    /**
     * Creates an optional dependency on the package.
     *
     * @param name  target package name
     * @return  dependency spec
     */
    public static PackageDependencySpec optional(String name) {
        return new PackageDependencySpec(name, OPTIONAL);
    }

    /**
     * Creates a passive dependency on the package.
     *
     * @param name  target package name
     * @return  dependency spec
     */
    public static PackageDependencySpec passive(String name) {
        return new PackageDependencySpec(name, PASSIVE);
    }

    public static PackageDependencySpec newInstance(String packageName, int type) throws ProvisioningDescriptionException {
        switch(type) {
            case PackageDependencySpec.OPTIONAL:
                return PackageDependencySpec.optional(packageName);
            case PackageDependencySpec.PASSIVE:
                return PackageDependencySpec.passive(packageName);
            case PackageDependencySpec.REQUIRED:
                return PackageDependencySpec.required(packageName);
            default:
                throw new ProvisioningDescriptionException(Errors.unexpectedPackageDependencyType(packageName, type));
        }
    }

    private final String name;
    private final int type;

    protected PackageDependencySpec(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return (type & OPTIONAL) == OPTIONAL;
    }

    public boolean isPassive() {
        return (type & PASSIVE) == PASSIVE;
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + type;
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
        PackageDependencySpec other = (PackageDependencySpec) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(name);
        if(isOptional()) {
            buf.append(type == OPTIONAL ? " optional" : " passive");
        }
        return buf.append(']').toString();
    }

    @Override
    public int compareTo(PackageDependencySpec o) {
        return name.compareTo(o.name);
    }
}
