/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.core.builder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.GalleonFeaturePackLayout;
import org.jboss.galleon.api.GalleonProvisioningLayout;
import org.jboss.galleon.api.GalleonProvisioningRuntime;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilderItf;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.FeaturePackLocation;

public interface ProvisioningContext extends AutoCloseable {

    public String getCoreVersion();

    public GalleonProvisioningConfig getConfig(GalleonProvisioningConfig config) throws ProvisioningException;

    public void storeProvisioningConfig(GalleonProvisioningConfig config, Path target) throws XMLStreamException, IOException, ProvisioningException;

    public GalleonProvisioningRuntime getProvisioningRuntime(GalleonProvisioningConfig config) throws ProvisioningException;

    UniverseResolver getUniverseResolver();

    public default void provision(GalleonProvisioningConfig config) throws ProvisioningException {
        provision(config, Collections.emptyList(), Collections.emptyMap());
    }

    public void provision(GalleonProvisioningConfig config, List<Path> customConfigs, Map<String, String> options) throws ProvisioningException;

    public void provision(Path config, Map<String, String> options) throws ProvisioningException;

    public GalleonProvisioningConfig parseProvisioningFile(Path provisioning) throws ProvisioningException;

    public List<GalleonFeaturePackLayout> getOrderedFeaturePackLayouts(GalleonProvisioningConfig config) throws ProvisioningException;

    public Set<String> getOrderedFeaturePackPluginLocations(GalleonProvisioningConfig config) throws ProvisioningException;

    /**
     * When dealing with parsed configuration that we want to update.
     */
    public GalleonConfigurationWithLayersBuilderItf buildConfigurationBuilder(GalleonConfigurationWithLayers config);

    public List<String> getInstalledPacks(Path dir) throws ProvisioningException;

    public GalleonProvisioningConfig loadProvisioningConfig(InputStream is) throws ProvisioningException, XMLStreamException;

    public FsDiff getFsDiff() throws ProvisioningException;

    public void install(FeaturePackLocation loc) throws ProvisioningException;

    public void install(GalleonFeaturePackConfig config) throws ProvisioningException;

    public void uninstall(FeaturePackLocation.FPID loc) throws ProvisioningException;

    public boolean hasOrderedFeaturePacksConfig(GalleonProvisioningConfig config, ConfigId cfg) throws ProvisioningException;

    public GalleonProvisioningLayout newProvisioningLayout(GalleonProvisioningConfig config) throws ProvisioningException;

    public GalleonProvisioningLayout newProvisioningLayout(Path file, boolean install) throws ProvisioningException;

    @Override
    public void close();
}
