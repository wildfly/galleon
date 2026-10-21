/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;

public interface GalleonFeaturePackRuntime {

    FeaturePackLocation.FPID getFPID();

    boolean hasPackages();

    boolean containsPackage(String name);

    Set<String> getPackageNames();

    Collection<GalleonPackageRuntime> getGalleonPackages();

    GalleonPackageRuntime getGalleonPackage(String name);

    Path getResource(String... path) throws ProvisioningDescriptionException;
}
