/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenLatestVersionNotAvailableException extends MavenUniverseException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final FeaturePackLocation fpl;

    /**
     *
     */
    public MavenLatestVersionNotAvailableException(String message) {
        super(message);
        fpl = null;
    }

    public MavenLatestVersionNotAvailableException(String message, FeaturePackLocation fpl) {
        super(message);
        this.fpl = fpl;
    }

    public FeaturePackLocation getLocation() {
        return fpl;
    }
}
