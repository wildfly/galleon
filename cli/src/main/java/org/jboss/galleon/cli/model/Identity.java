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
package org.jboss.galleon.cli.model;

import java.util.Objects;

import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.path.PathParser;

/**
 *
 * @author jdenise@redhat.com
 */
public class Identity implements Comparable<Identity> {
    private final String origin;
    private final String name;

    private Identity(String origin, String name) {
        this.origin = origin;
        this.name = name;
    }

    public static Identity fromGav(Gav origin, String name) {
        return new Identity(origin.getGroupId() + ":" + origin.getArtifactId(), name);
    }

    public static Identity fromString(String origin, String name) {
        return new Identity(origin, name);
    }

    public static String buildOrigin(Gav gav) {
        return gav.getGroupId() + ":" + gav.getArtifactId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Identity)) {
            return false;
        }
        Identity gi = (Identity) obj;
        return getName().equals(gi.getName()) && getOrigin().equals(gi.getOrigin());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.getOrigin());
        hash = 79 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    public String getOrigin() {
        return origin;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Identity o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return origin + PathParser.PATH_SEPARATOR + name;
    }
}
