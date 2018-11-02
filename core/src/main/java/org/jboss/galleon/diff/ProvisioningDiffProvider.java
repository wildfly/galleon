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

package org.jboss.galleon.diff;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningDiffProvider {

    public static ProvisioningDiffProvider newInstance(ProvisioningLayout<?> layout, ProvisionedState provisionedState, FsDiff diff, MessageWriter log) {
        ProvisioningDiffProvider diffProvider = new ProvisioningDiffProvider();
        diffProvider.layout = layout;
        diffProvider.provisioningConfig = layout.getConfig();
        diffProvider.provisionedState = provisionedState;
        diffProvider.fsDiff = diff;
        diffProvider.log = log;
        return diffProvider;
    }

    private ProvisioningLayout<?> layout;
    private ProvisioningConfig provisioningConfig;
    private ProvisionedState provisionedState;
    private FsDiff fsDiff;
    private MessageWriter log;

    private Map<FPID, FeaturePackConfig.Builder> updatedDirectFps = Collections.emptyMap();
    private Map<FPID, FeaturePackConfig.Builder> updatedTransitiveFps = Collections.emptyMap();
    private Map<FPID, FeaturePackConfig.Builder> addedTransitiveFps = Collections.emptyMap();
    private Map<ConfigId, ConfigModel> updatedConfigs = Collections.emptyMap();
    private Map<ConfigId, ConfigModel> addedConfigs = Collections.emptyMap();
    private Set<ConfigId> removedConfigs = Collections.emptySet();

    private ProvisioningConfig mergedConfig;

    private ProvisioningDiffProvider() {
    }

    public MessageWriter getMessageWriter() {
        return log;
    }

    public ProvisioningLayout<?> getProvisioningLayout() {
        return layout;
    }

    public ProvisioningConfig getOriginalConfig() {
        return provisioningConfig;
    }

    public ProvisionedState getProvisionedState() {
        return provisionedState;
    }

    public FsDiff getFsDiff() {
        return fsDiff;
    }

    public void excludePackage(FPID fpid, String name, String... relativePaths) throws ProvisioningException {
        getFpcBuilder(fpid).excludePackage(name);
        suppressPaths(relativePaths);
    }

    public void includePackage(FPID fpid, String name, String... relativePaths) throws ProvisioningException {
        getFpcBuilder(fpid).includePackage(name);
        suppressPaths(relativePaths);
    }

    public void updateConfig(ConfigModel config, String... relativePaths) throws ProvisioningException {
        updatedConfigs = CollectionUtils.put(updatedConfigs, config.getId(), config);
        suppressPaths(relativePaths);
    }

    public void addConfig(ConfigModel config, String... relativePaths) throws ProvisioningException {
        addedConfigs = CollectionUtils.putLinked(addedConfigs, config.getId(), config);
        suppressPaths(relativePaths);
    }

    public void removeConfig(ConfigId configId, String... relativePaths) throws ProvisioningException {
        removedConfigs = CollectionUtils.add(removedConfigs, configId);
        suppressPaths(relativePaths);
    }

    public boolean hasConfigChanges() {
        return !updatedDirectFps.isEmpty() ||
                !updatedTransitiveFps.isEmpty() ||
                !addedTransitiveFps.isEmpty() ||
                !updatedConfigs.isEmpty() ||
                !addedConfigs.isEmpty() ||
                !removedConfigs.isEmpty();
    }

    public ProvisioningConfig getMergedConfig() throws ProvisioningException {
        if(mergedConfig != null) {
            return mergedConfig;
        }
        if(!hasConfigChanges()) {
            mergedConfig = provisioningConfig;
            return provisioningConfig;
        }

        final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder();
        configBuilder.initUniverses(provisioningConfig);
        if(provisioningConfig.hasPluginOptions()) {
            configBuilder.addOptions(provisioningConfig.getPluginOptions());
        }

        for(FeaturePackConfig fp : provisioningConfig.getFeaturePackDeps()) {
            final FeaturePackConfig.Builder fpcBuilder = updatedDirectFps.get(fp.getLocation().getFPID());
            if(fpcBuilder == null) {
                configBuilder.addFeaturePackDep(provisioningConfig.originOf(fp.getLocation().getProducer()), fp);
            } else {
                configBuilder.addFeaturePackDep(provisioningConfig.originOf(fp.getLocation().getProducer()), fpcBuilder.build());
            }
        }

        for (FeaturePackConfig fp : provisioningConfig.getTransitiveDeps()) {
            final FeaturePackConfig.Builder fpcBuilder = updatedTransitiveFps.get(fp.getLocation().getFPID());
            if (fpcBuilder == null) {
                configBuilder.addFeaturePackDep(provisioningConfig.originOf(fp.getLocation().getProducer()), fp);
            } else {
                configBuilder.addFeaturePackDep(provisioningConfig.originOf(fp.getLocation().getProducer()),
                        fpcBuilder.build());
            }
        }

        for (FeaturePackConfig.Builder fpcBuilder : addedTransitiveFps.values()) {
            configBuilder.addFeaturePackDep(fpcBuilder.build());
        }

        for (ConfigModel originalConfig : provisioningConfig.getDefinedConfigs()) {
            ConfigModel config = updatedConfigs.get(originalConfig.getId());
            if (config != null) {
                configBuilder.addConfig(config);
                continue;
            }
            if (removedConfigs.contains(originalConfig.getId())) {
                continue;
            }
            configBuilder.addConfig(originalConfig);
        }

        if (!addedConfigs.isEmpty()) {
            for (ConfigModel config : addedConfigs.values()) {
                configBuilder.addConfig(config);
            }
        }
        mergedConfig = configBuilder.build();
        return mergedConfig;
    }

    private void suppressPaths(String... relativePaths) throws ProvisioningException {
        for (String relativePath : relativePaths) {
            fsDiff.suppress(relativePath);
        }
    }

    private FeaturePackConfig.Builder getFpcBuilder(FPID fpid) {
        FeaturePackConfig.Builder fpcBuilder = updatedDirectFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }
        fpcBuilder = updatedTransitiveFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }
        fpcBuilder = addedTransitiveFps.get(fpid);
        if(fpcBuilder != null) {
            return fpcBuilder;
        }

        FeaturePackConfig fpc = provisioningConfig.getFeaturePackDep(fpid.getProducer());
        if(fpc != null) {
            fpcBuilder = FeaturePackConfig.builder(fpc);
            updatedDirectFps = CollectionUtils.put(updatedDirectFps, fpid, fpcBuilder);
            return fpcBuilder;
        }

        fpc = provisioningConfig.getTransitiveDep(fpid.getProducer());
        if(fpc != null) {
            fpcBuilder = FeaturePackConfig.builder(fpc);
            updatedTransitiveFps = CollectionUtils.put(updatedTransitiveFps, fpid, fpcBuilder);
            return fpcBuilder;
        }

        fpcBuilder = FeaturePackConfig.transitiveBuilder(fpid.getLocation());
        addedTransitiveFps = CollectionUtils.putLinked(addedTransitiveFps, fpid, fpcBuilder);
        return fpcBuilder;
    }
}
