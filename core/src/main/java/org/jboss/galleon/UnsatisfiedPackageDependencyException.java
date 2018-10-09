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
