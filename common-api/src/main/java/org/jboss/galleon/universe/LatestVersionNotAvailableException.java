/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public class LatestVersionNotAvailableException extends ProvisioningException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final FeaturePackLocation fpl;

    /**
     * @param fpl  feature-pack location
     * @param cause  the actual cause
     */
    public LatestVersionNotAvailableException(FeaturePackLocation fpl, Throwable cause) {
        super(BaseErrors.noVersionAvailable(fpl), cause);
        this.fpl = fpl;
    }

    /**
     * @param fpl  feature-pack location
     */
    public LatestVersionNotAvailableException(FeaturePackLocation fpl) {
        super(BaseErrors.noVersionAvailable(fpl));
        this.fpl = fpl;
    }

    public FeaturePackLocation getLocation() {
        return fpl;
    }
}
