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
package org.jboss.galleon.api;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilderItf;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.impl.ProvisioningUtil;
import org.jboss.galleon.progresstracking.ProgressCallback;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;

public interface Provisioning extends AutoCloseable {

    @Override
    public void close();

    /**
     * Location of the installation.
     *
     * @return location of the installation
     */
    public Path getInstallationHome();

    /**
     * Whether to log provisioning time
     *
     * @return Whether provisioning time should be logged at the end
     */
    public boolean isLogTime();

    /**
     * Whether provisioning state will be recorded after (re-)provisioning.
     *
     * @return true if the provisioning state is recorded after provisioning,
     * otherwise false
     */
    public boolean isRecordState();

    public static boolean isFeaturePack(Path path) {
        return ProvisioningUtil.isFeaturePack(path);
    }

    public static GalleonFeaturePackDescription getFeaturePackDescription(Path path) throws ProvisioningException {
        return ProvisioningUtil.getFeaturePackDescription(path);
    }

    // Required by CLI
    /**
     * Add named universe spec to the provisioning configuration
     *
     * @param name universe name
     * @param universeSpec universe spec
     * @throws ProvisioningException in case of an error
     */
    public void addUniverse(String name, UniverseSpec universeSpec) throws ProvisioningException;

    /**
     * Removes universe spec associated with the name from the provisioning
     * configuration
     *
     * @param name name of the universe spec or null for the default universe
     * spec
     * @throws ProvisioningException in case of an error
     */
    public void removeUniverse(String name) throws ProvisioningException;

    /**
     * Set the default universe spec for the installation
     *
     * @param universeSpec universe spec
     * @throws ProvisioningException in case of an error
     */
    public void setDefaultUniverse(UniverseSpec universeSpec) throws ProvisioningException;

    public GalleonProvisioningConfig getProvisioningConfig() throws ProvisioningException;

    public void setProgressCallback(String id, ProgressCallback<?> callback);

    public void setProgressTracker(String id, ProgressTracker<?> tracker);

    public List<String> getInstalledPacks(Path dir) throws ProvisioningException;

    public default GalleonProvisioningConfig loadProvisioningConfig(Path file) throws ProvisioningException {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
                return loadProvisioningConfig(fileInputStream);
            }
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }

    public GalleonProvisioningConfig loadProvisioningConfig(InputStream is) throws ProvisioningException;

    public void storeProvisioningConfig(GalleonProvisioningConfig config, Path target) throws ProvisioningException;

    public default void provision(GalleonProvisioningConfig config) throws ProvisioningException {
        provision(config, Collections.emptyList(), Collections.emptyMap());
    }

    public default void provision(GalleonProvisioningConfig config, Map<String, String> options) throws ProvisioningException {
        provision(config, Collections.emptyList(), options);
    }

    public void provision(GalleonProvisioningConfig config, List<Path> customConfigs, Map<String, String> options) throws ProvisioningException;

    public default void provision(Path config) throws ProvisioningException {
        provision(config, Collections.emptyMap());
    }

    public void provision(Path config, Map<String, String> options) throws ProvisioningException;

    public GalleonProvisioningLayout newProvisioningLayout(GalleonProvisioningConfig config) throws ProvisioningException;

    public GalleonProvisioningLayout newProvisioningLayout(Path file, boolean install) throws ProvisioningException;

    public GalleonProvisioningRuntime getProvisioningRuntime(GalleonProvisioningConfig config) throws ProvisioningException;

    public UniverseResolver getUniverseResolver();

    /**
     * When dealing with parsed configuration that we want to update.
     */
    public GalleonConfigurationWithLayersBuilderItf buildConfigurationBuilder(GalleonConfigurationWithLayers config) throws ProvisioningException;

    public boolean hasOrderedFeaturePacksConfig(GalleonProvisioningConfig config, ConfigId cfg) throws ProvisioningException;

    public Set<String> getOrderedFeaturePackPluginLocations(GalleonProvisioningConfig config) throws ProvisioningException;

    public FsDiff getFsDiff() throws ProvisioningException;

    public void install(FeaturePackLocation loc) throws ProvisioningException;
    public void install(GalleonFeaturePackConfig config) throws ProvisioningException;
    public void uninstall(FeaturePackLocation.FPID loc) throws ProvisioningException;
}
