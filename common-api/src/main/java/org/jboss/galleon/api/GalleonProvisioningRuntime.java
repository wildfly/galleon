/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
