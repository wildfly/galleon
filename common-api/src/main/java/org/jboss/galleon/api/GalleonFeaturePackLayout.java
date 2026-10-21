/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

public interface GalleonFeaturePackLayout {

    public Path getDir();

    public int getType();

    public boolean isDirectDep();

    public boolean isTransitiveDep();

    public boolean isPatch();

    public boolean hasTransitiveDep(ProducerSpec spec) throws ProvisioningException;

    public boolean hasFeaturePackDep(ProducerSpec spec) throws ProvisioningException;

    public FPID getPatchFor() throws ProvisioningException;

    public FeaturePackLocation.FPID getFPID();

    public Path getResource(String... path) throws ProvisioningDescriptionException;

    public Set<ConfigId> loadLayers() throws ProvisioningException, IOException;

    public GalleonConfigurationWithLayers loadModel(String model) throws ProvisioningException;

    public GalleonLayer loadLayer(String model, String name) throws ProvisioningException;

    public List<FPID> getFeaturePackDeps() throws ProvisioningException;
}
