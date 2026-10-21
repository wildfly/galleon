/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon;

import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 * Represents an error caused by an unsatisfied dependency on a package in a feature-pack.
 *
 * @author Alexey Loubyansky
 */
public class UnsatisfiedPackageDependencyException extends ProvisioningDescriptionException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception representing an unsatisfied package dependency error.
     *
     * @param fpid  the feature-pack that is expected to contain the package
     * @param packageName  the name of the package dependency on which couldn't be resolved
     */
    public UnsatisfiedPackageDependencyException(FPID fpid, String packageName) {
        super(Errors.unsatisfiedPackageDependency(fpid, packageName));
    }

    /**
     * Creates an exception representing an unsatisfied package dependency error.
     *
     * @param fpid  the feature-pack that is expected to contain the package
     * @param packageName  the name of the package dependency on which couldn't be resolved
     * @param cause  original cause
     */
    public UnsatisfiedPackageDependencyException(FPID fpid, String packageName, UnsatisfiedPackageDependencyException cause) {
        super(Errors.unsatisfiedPackageDependency(fpid, packageName), cause);
    }
}
