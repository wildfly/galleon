/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;

public interface GalleonPackageRuntime {

    String getStability();

    String getName();

    /**
     * Returns a resource path for a package.
     *
     * @param path  path to the resource relative to the package resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack or package were not found in the layout
     */
    Path getResource(String... path) throws ProvisioningDescriptionException;

    Path getContentDir();

    FeaturePackLocation.FPID getFeaturePackFPID();
}
