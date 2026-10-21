/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import java.util.Objects;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class Identity implements Comparable<Identity> {
    public static final String EMPTY = "";
    private final String origin;
    private final String name;

    private Identity(String origin, String name) {
        this.origin = origin;
        this.name = name;
    }

    public static Identity fromChannel(ProducerSpec origin, String name) {
        return new Identity(FeaturePackConfig.getDefaultOriginName(origin.getLocation()), name);
    }

    public static Identity fromString(String origin, String name) {
        return new Identity(origin, name);
    }

    public static Identity fromString(String name) {
        return new Identity(EMPTY, name);
    }

    public static String buildOrigin(ProducerSpec producer) {
        return FeaturePackConfig.getDefaultOriginName(producer.getLocation());
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
