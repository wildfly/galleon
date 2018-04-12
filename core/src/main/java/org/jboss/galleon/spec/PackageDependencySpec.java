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
package org.jboss.galleon.spec;

/**
 * Describes dependency on a single package.
 *
 * @author Alexey Loubyansky
 */
public class PackageDependencySpec implements Comparable<PackageDependencySpec> {

    /**
     * Creates a required dependency on the provided package name.
     *
     * @param name  target package name
     * @return  dependency description
     */
    public static PackageDependencySpec forPackage(String name) {
        return new PackageDependencySpec(name, false);
    }

    /**
     * Creates a dependency on the provided package name.
     *
     * @param name  target package name
     * @param optional  whether the dependency should be optional
     * @return  dependency description
     */
    public static PackageDependencySpec forPackage(String name, boolean optional) {
        return new PackageDependencySpec(name, optional);
    }

    private final String name;
    private final boolean optional;

    protected PackageDependencySpec(String name, boolean optional) {
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
        PackageDependencySpec other = (PackageDependencySpec) obj;
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
    public int compareTo(PackageDependencySpec o) {
        return name.compareTo(o.name);
    }
}
