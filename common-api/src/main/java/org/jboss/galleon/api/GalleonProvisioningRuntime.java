/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

public interface GalleonProvisioningRuntime extends AutoCloseable {

    boolean isLogTime();

    /**
     * The target staged location
     *
     * @return the staged location
     */
    Path getStagedDir();

    boolean hasFeaturePacks();

    boolean hasFeaturePack(ProducerSpec producer);

    Collection<GalleonFeaturePackRuntime> getGalleonFeaturePacks();

    public Collection<GalleonProvisionedConfig> getGalleonConfigs();

    /**
     * Returns a resource path for the provisioning setup.
     *
     * @param path  path to the resource relative to the global resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningException  in case of a failure
     */
    Path getResource(String... path) throws ProvisioningException;

    void provision() throws ProvisioningException;

    List<GalleonFeatureSpec> getAllFeatures() throws ProvisioningException;

    @Override
    void close();
}
