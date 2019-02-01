/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.Constants;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.UnsatisfiedPackageDependencyException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigItem;
import org.jboss.galleon.config.ConfigItemContainer;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeatureGroupSupport;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.PackageConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.layout.FeaturePackLayoutFactory;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.spec.ConfigLayerDependency;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageDepsSpec;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;


/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningRuntimeBuilder {

    public static final FeaturePackLayoutFactory<FeaturePackRuntimeBuilder> FP_RT_FACTORY = new FeaturePackLayoutFactory<FeaturePackRuntimeBuilder>() {
        @Override
        public FeaturePackRuntimeBuilder newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path fpDir, int type) {
            return new FeaturePackRuntimeBuilder(fpl.getFPID(), spec, fpDir, type);
        }
    };

    public static ProvisioningRuntimeBuilder newInstance() {
        return newInstance(DefaultMessageWriter.getDefaultInstance());
    }

    public static ProvisioningRuntimeBuilder newInstance(final MessageWriter messageWriter) {
        return new ProvisioningRuntimeBuilder(messageWriter);
    }

    final long startTime;
    String encoding;
    ProvisioningConfig config;
    ProvisioningLayout<FeaturePackRuntimeBuilder> layout;
    Path stagedDir;
    FsDiff fsDiff;
    private final MessageWriter messageWriter;

    Map<String, ConfigModelStack> nameOnlyConfigs = Collections.emptyMap();
    Map<String, Map<String, ConfigModelStack>> namedModelConfigs = Collections.emptyMap();

    private FeaturePackRuntimeBuilder thisOrigin;
    private FeaturePackRuntimeBuilder currentOrigin;
    private ConfigModelStack configStack;

    private FpStack fpConfigStack;

    private ResolvedFeature parentFeature;

    private Map<ConfigId, ConfigModelStack> configsToBuild = Collections.emptyMap();
    private Map<ConfigId, ConfigModelStack> layers = Collections.emptyMap();

    private ArrayList<PackageRuntime.Builder> resolvedPkgBranch = new ArrayList<>();
    int pkgsTotal;

    private List<FeaturePackRuntimeBuilder> visited = new ArrayList<>();
    private int pkgDepMask;
    int includedPkgDeps;

    private ProvisioningRuntimeBuilder(final MessageWriter messageWriter) {
        startTime = System.currentTimeMillis();
        this.messageWriter = messageWriter;
    }

    public ProvisioningRuntimeBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public ProvisioningRuntimeBuilder initRtLayout(ProvisioningLayout<FeaturePackRuntimeBuilder> configLayout) throws ProvisioningException {
        layout = configLayout;
        return this;
    }

    public ProvisioningRuntimeBuilder initLayout(ProvisioningLayout<?> configLayout) throws ProvisioningException {
        layout = configLayout.transform(FP_RT_FACTORY);
        return this;
    }

    public ProvisioningRuntimeBuilder initLayout(ProvisioningLayoutFactory layoutFactory, ProvisioningConfig config) throws ProvisioningException {
        layout = layoutFactory.newConfigLayout(config, FP_RT_FACTORY, false);
        return this;
    }

    public ProvisioningRuntimeBuilder setFsDiff(FsDiff fsDiff) {
        this.fsDiff = fsDiff;
        return this;
    }

    public ProvisioningRuntimeBuilder setStagedDir(Path p) {
        this.stagedDir = p;
        return this;
    }

    public ProvisioningRuntime build() throws ProvisioningException {
        try {
            return doBuild();
        } catch(ProvisioningException | RuntimeException | Error e) {
            throw e;
        } finally {
            layout.close();
        }
    }

    static final int PKG_DEP_MASK_ALL = Integer.MAX_VALUE;
    static final int PKG_DEP_MASK_PASSIVE = PKG_DEP_MASK_ALL ^ PackageDependencySpec.OPTIONAL;
    static final int PKG_DEP_MASK_REQUIRED = PKG_DEP_MASK_ALL ^ PackageDependencySpec.PASSIVE;

    static final int PKG_DEP_ALL = 0;
    static final int PKG_DEP_PASSIVE = 1;
    static final int PKG_DEP_PASSIVE_PLUS = 2;
    static final int PKG_DEP_REQUIRED = 3;

    private ProvisioningRuntime doBuild() throws ProvisioningException {

        config = layout.getConfig();
        fpConfigStack = new FpStack(config);

        final String optionalPackages = layout.getOptionValue(ProvisioningOption.OPTIONAL_PACKAGES);
        switch(optionalPackages) {
            case Constants.ALL:
                pkgDepMask = PKG_DEP_MASK_ALL;
                includedPkgDeps = PKG_DEP_ALL;
                break;
            case Constants.PASSIVE_PLUS:
                pkgDepMask = PKG_DEP_MASK_ALL;
                includedPkgDeps = PKG_DEP_PASSIVE_PLUS;
                break;
            case Constants.PASSIVE:
                pkgDepMask = PKG_DEP_MASK_PASSIVE;
                includedPkgDeps = PKG_DEP_PASSIVE;
                break;
            case Constants.NONE:
                pkgDepMask = PKG_DEP_MASK_REQUIRED;
                includedPkgDeps = PKG_DEP_REQUIRED;
                break;
            default:
                throw new ProvisioningDescriptionException(Errors.pluginOptionIllegalValue(ProvisioningOption.OPTIONAL_PACKAGES.getName(), optionalPackages, ProvisioningOption.OPTIONAL_PACKAGES.getValueSet()));
        }

        collectDefaultConfigs();

        List<ConfigModelStack> configStacks = Collections.emptyList();
        if(config.hasDefinedConfigs()) {
            for(ConfigModel config : config.getDefinedConfigs()) {
                if (config.getId().isModelOnly()/* || fpConfigStack.isFilteredOut(null, config.getId(), true)*/) {
                    continue;
                }
                configStack = configsToBuild.get(config.getId());
                configStack.pushConfig(config);
                configStacks = CollectionUtils.add(configStacks, configStack);
            }
        }

        final Collection<FeaturePackConfig> fpConfigs = config.getFeaturePackDeps();
        boolean extendedStackLevel = false;
        if(config.hasTransitiveDeps()) {
            for (FeaturePackConfig fpConfig : config.getTransitiveDeps()) {
                extendedStackLevel |= fpConfigStack.push(fpConfig, extendedStackLevel);
            }
        }
        for (FeaturePackConfig fpConfig : fpConfigs) {
            extendedStackLevel |= fpConfigStack.push(fpConfig, extendedStackLevel);
        }
        while(fpConfigStack.hasNext()) {
            processFpConfig(fpConfigStack.next());
        }

        for(int i = configStacks.size() - 1; i>= 0; --i) {
            final ConfigModelStack configStack = configStacks.get(i);
            processConfig(configStack, popConfig(configStack));
        }

        if(extendedStackLevel) {
            fpConfigStack.popLevel();
        }

        mergeModelOnlyConfigs();

        return new ProvisioningRuntime(this, messageWriter);
    }

    private void mergeModelOnlyConfigs() throws ProvisioningException {
        if(namedModelConfigs.isEmpty()) {
            return;
        }
        for(Map.Entry<String, Map<String, ConfigModelStack>> entry : namedModelConfigs.entrySet()) {
            final ConfigId modelOnlyId = new ConfigId(entry.getKey(), null);
            final ConfigModelStack modelOnlyStack = resolveModelOnlyConfig(modelOnlyId);
            if(modelOnlyStack == null) {
                continue;
            }
            for(ConfigModelStack configStack : entry.getValue().values()) {
                configStack.merge(modelOnlyStack);
            }
        }
    }

    private void collectDefaultConfigs() throws ProvisioningException {
        if(config.hasDefinedConfigs()) {
            for(ConfigModel config : config.getDefinedConfigs()) {
                final ConfigId id = config.getId();
                if (id.isModelOnly()/* || fpConfigStack.isFilteredOut(null, id, true)*/) {
                    continue;
                }
                ConfigModelStack configStack = configsToBuild.get(id);
                if(configStack == null) {
                    configStack = getConfigStack(id);
                    configsToBuild = CollectionUtils.putLinked(configsToBuild, id, configStack);
                }
            }
        }

        final Collection<FeaturePackConfig> fpConfigs = config.getFeaturePackDeps();
        boolean extendedStackLevel = false;
        if(config.hasTransitiveDeps()) {
            for (FeaturePackConfig fpConfig : config.getTransitiveDeps()) {
                extendedStackLevel |= fpConfigStack.push(fpConfig, extendedStackLevel);
            }
        }
        for (FeaturePackConfig fpConfig : fpConfigs) {
            extendedStackLevel |= fpConfigStack.push(fpConfig, extendedStackLevel);
        }
        while(fpConfigStack.hasNext()) {
            collectDefaultConfigs(fpConfigStack.next());
        }
        if(extendedStackLevel) {
            fpConfigStack.popLevel();
        }
    }

    private void collectDefaultConfigs(FeaturePackConfig fpConfig) throws ProvisioningException {
        final ProducerSpec producer = fpConfig.getLocation().getProducer();
        thisOrigin = layout.getFeaturePack(producer);
        final FeaturePackRuntimeBuilder parentFp = setOrigin(thisOrigin);
        try {
            if (fpConfig.hasDefinedConfigs()) {
                for(ConfigModel config : fpConfig.getDefinedConfigs()) {
                    final ConfigId id = config.getId();
                    if(id.isModelOnly() || fpConfigStack.isFilteredOut(producer, id, true)) {
                        continue;
                    }
                    ConfigModelStack configStack = configsToBuild.get(id);
                    if(configStack == null) {
                        configStack = getConfigStack(id);
                        configsToBuild = CollectionUtils.putLinked(configsToBuild, id, configStack);
                    }
                }
            }

            boolean extendedStackLevel = false;
            if (!fpConfig.isTransitive()) {
                final FeaturePackSpec currentSpec = currentOrigin.getSpec();
                if (currentSpec.hasDefinedConfigs()) {
                    for (ConfigModel config : currentSpec.getDefinedConfigs()) {
                        final ConfigId id = config.getId();
                        if (id.isModelOnly() || fpConfigStack.isFilteredOut(producer, id, false)) {
                            continue;
                        }
                        ConfigModelStack configStack = configsToBuild.get(id);
                        if (configStack == null) {
                            configStack = getConfigStack(id);
                            configsToBuild = CollectionUtils.putLinked(configsToBuild, id, configStack);
                        }
                    }
                }

                if (currentSpec.hasFeaturePackDeps()) {
                    if (currentSpec.hasTransitiveDeps()) {
                        for (FeaturePackConfig fpDep : currentSpec.getTransitiveDeps()) {
                            extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                        }
                    }
                    for (FeaturePackConfig fpDep : currentSpec.getFeaturePackDeps()) {
                        extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                    }
                    if (extendedStackLevel) {
                        while (fpConfigStack.hasNext()) {
                            collectDefaultConfigs(fpConfigStack.next());
                        }
                    }
                }
            }
            if (extendedStackLevel) {
                fpConfigStack.popLevel();
            }
        } finally {
            this.thisOrigin = parentFp;
            setOrigin(parentFp);
        }
    }

    private void processFpConfig(FeaturePackConfig fpConfig) throws ProvisioningException {
        final ProducerSpec producer = fpConfig.getLocation().getProducer();
        thisOrigin = layout.getFeaturePack(producer);
        final FeaturePackRuntimeBuilder parentFp = setOrigin(thisOrigin);

        try {
            List<ConfigModelStack> fpConfigStacks = Collections.emptyList();
            List<ConfigModelStack> specConfigStacks = Collections.emptyList();
            for(Map.Entry<ConfigId, ConfigModelStack> entry : configsToBuild.entrySet()) {
                final ConfigId configId = entry.getKey();
                configStack = entry.getValue();

                ConfigModel config = fpConfig.getDefinedConfig(configId);
                if (config != null && !fpConfigStack.isFilteredOut(producer, configId, true)) {
                    configStack.pushConfig(config);
                    fpConfigStacks = CollectionUtils.add(fpConfigStacks, configStack);
                }

                if (fpConfig.isTransitive() || fpConfigStack.isFilteredOut(producer, configId, false)) {
                    continue;
                }

                config = currentOrigin.getConfig(configId);
                if (config != null) {
                    configStack.pushConfig(config);
                    specConfigStacks = CollectionUtils.add(specConfigStacks, configStack);
                }

                config = currentOrigin.getSpec().getDefinedConfig(configId);
                if(config != null) {
                    configStack.pushConfig(config);
                    specConfigStacks = CollectionUtils.add(specConfigStacks, configStack);
                }
            }
            configStack = null;

            boolean extendedStackLevel = false;
            if (!fpConfig.isTransitive()) {
                if (currentOrigin.getSpec().hasFeaturePackDeps()) {
                    if (currentOrigin.getSpec().hasTransitiveDeps()) {
                        for (FeaturePackConfig fpDep : currentOrigin.getSpec().getTransitiveDeps()) {
                            extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                        }
                    }
                    for (FeaturePackConfig fpDep : currentOrigin.getSpec().getFeaturePackDeps()) {
                        extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                    }
                    if (extendedStackLevel) {
                        while (fpConfigStack.hasNext()) {
                            processFpConfig(fpConfigStack.next());
                        }
                    }
                }

                if (!specConfigStacks.isEmpty()) {
                    for (int i = specConfigStacks.size() - 1; i >= 0; --i) {
                        final ConfigModelStack configStack = specConfigStacks.get(i);
                        processConfig(configStack, popConfig(configStack));
                    }
                }

                if (fpConfig.isInheritPackages() && currentOrigin.getSpec().hasDefaultPackages()) {
                    for (String packageName : currentOrigin.getSpec().getDefaultPackageNames()) {
                        if (fpConfigStack.isPackageFilteredOut(currentOrigin.producer, packageName, false)) {
                            continue;
                        }
                        resolvePackage(packageName, null, PackageDependencySpec.REQUIRED);
                    }
                }
            }

            if (fpConfig.hasIncludedPackages()) {
                for (PackageConfig pkgConfig : fpConfig.getIncludedPackages()) {
                    if (fpConfigStack.isPackageFilteredOut(currentOrigin.producer, pkgConfig.getName(), true)) {
                        continue;
                    }
                    resolvePackage(pkgConfig.getName(), null, PackageDependencySpec.REQUIRED);
                }
            }

            if(!fpConfigStacks.isEmpty()) {
                for (int i = fpConfigStacks.size() - 1; i >= 0; --i) {
                    final ConfigModelStack configStack = fpConfigStacks.get(i);
                    processConfig(configStack, popConfig(configStack));
                }
            }

            if (extendedStackLevel) {
                fpConfigStack.popLevel();
            }
        } finally {
            this.thisOrigin = parentFp;
            setOrigin(parentFp);
        }
    }

    private ConfigModel popConfig(ConfigModelStack configStack) throws ProvisioningException {
        final ConfigModel config = configStack.peekAtConfig();
        if(config.hasIncludedLayers()) {
            for(String dep : config.getIncludedLayers()) {
                if(configStack.isLayerFilteredOut(dep)) {
                    continue;
                }
                includeLayer(configStack, new ConfigId(config.getModel(), dep));
            }
        }
        return configStack.popConfig();
    }

    private void includeLayer(ConfigModelStack configStack, ConfigId layerId) throws ProvisioningException {
        if(!configStack.addLayer(layerId)) {
            return;
        }
        final ConfigModelStack layerStack = resolveConfigLayer(layerId);
        if(layerStack.hasLayerDeps()) {
            for(ConfigLayerDependency layerDep : layerStack.getLayerDeps()) {
                if(configStack.isLayerExcluded(layerDep.getName())) {
                    if(layerDep.isOptional()) {
                        continue;
                    }
                    throw new ProvisioningException(Errors.unsatisfiedLayerDependency(layerId.getName(), layerDep.getName()));
                }
                includeLayer(configStack, new ConfigId(configStack.id.getModel(), layerDep.getName()));
            }
        }
        configStack.includedLayer(layerId);
        for(ResolvedFeature feature : layerStack.orderFeatures(false)) {
            if(configStack.isFilteredOut(feature.getSpecId(), feature.getId())) {
                continue;
            }
            configStack.includeFeature(feature.id, feature.spec, feature.params,
                    feature.deps, feature.unsetParams, feature.resetParams);
        }
    }

    private ConfigModelStack resolveConfigLayer(ConfigId layerId) throws ProvisioningException {
        ConfigModelStack layerStack = layers.get(layerId);
        if(layerStack == null) {
            layerStack = new ConfigModelStack(layerId, this);
            boolean resolved = false;
            for (FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                resolved |= resolveConfigLayer(layout.getFeaturePack(fpConfig.getLocation().getProducer()), layerStack, layerId);
            }
            if(!resolved) {
                throw new ProvisioningException(Errors.layerNotFound(layerId));
            }
            layers = CollectionUtils.put(layers, layerId, layerStack);
        }
        return layerStack;
    }

    private boolean resolveConfigLayer(FeaturePackRuntimeBuilder fp, ConfigModelStack layerStack, ConfigId layerId) throws ProvisioningException {
        final FeaturePackRuntimeBuilder prevOrigin = currentOrigin;
        try {
            boolean resolved;
            final ConfigLayerSpec configLayer = fp.getConfigLayer(layerId);
            resolved = configLayer != null;
            if (resolved) {
                layerStack.pushGroup(configLayer);
            }
            if(fp.getSpec().hasFeaturePackDeps()) {
                for(FeaturePackConfig depConfig : fp.getSpec().getFeaturePackDeps()) {
                    resolved |= resolveConfigLayer(layout.getFeaturePack(depConfig.getLocation().getProducer()), layerStack, layerId);
                }
            }
            if(configLayer != null) {
                setOrigin(fp);
                processConfigLayer(layerStack, popLayer(layerStack));
            }
            return resolved;
        } finally {
            setOrigin(prevOrigin);
        }
    }

    private FeatureGroupSupport popLayer(ConfigModelStack layerStack) throws ProvisioningException {
        final FeatureGroupSupport fg = layerStack.peekAtGroup();
        if(!(fg instanceof ConfigLayerSpec)) {
            throw new ProvisioningException("Expected config layer but got " + fg);
        }
        final ConfigLayerSpec layer = (ConfigLayerSpec) fg;
        if(layer.hasLayerDeps()) {
            for(ConfigLayerDependency dep : layer.getLayerDeps()) {
                layerStack.addLayerDep(dep);
            }
        }
        layerStack.popGroup();
        return fg;
    }

    private ConfigModelStack resolveModelOnlyConfig(ConfigId configId) throws ProvisioningException {
        boolean extendedStackLevel = false;
        if (config.hasTransitiveDeps()) {
            for (FeaturePackConfig fpDep : config.getTransitiveDeps()) {
                extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
            }
        }
        ConfigModelStack modelOnlyStack = null;
        for (FeaturePackConfig fpDep : config.getFeaturePackDeps()) {
            extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
            if(fpDep.isConfigModelExcluded(configId) || !fpDep.isInheritModelOnlyConfigs() && !fpDep.isConfigModelIncluded(configId)) {
                continue;
            }
            modelOnlyStack = resolveModelOnlyConfig(fpDep, modelOnlyStack, configId);
        }
        if (extendedStackLevel) {
            fpConfigStack.popLevel();
        }
        return modelOnlyStack;
    }

    private ConfigModelStack resolveModelOnlyConfig(FeaturePackConfig fpConfig, ConfigModelStack modelOnlyStack, ConfigId configId) throws ProvisioningException {
        final FeaturePackRuntimeBuilder fp = layout.getFeaturePack(fpConfig.getLocation().getProducer());
        final FeaturePackRuntimeBuilder prevOrigin = currentOrigin;
        try {
            int pushedCount = 0;
            ConfigModel config = fpConfig.getDefinedConfig(configId);
            if(config != null) {
                if(modelOnlyStack == null) {
                    modelOnlyStack = new ConfigModelStack(configId, this);
                }
                modelOnlyStack.pushConfig(config);
                ++pushedCount;
            }
            config = fp.getSpec().getDefinedConfig(configId);
            if(config != null) {
                if(modelOnlyStack == null) {
                    modelOnlyStack = new ConfigModelStack(configId, this);
                }
                modelOnlyStack.pushConfig(config);
                ++pushedCount;
            }
            config = fp.getConfig(configId);
            if (config != null) {
                if(modelOnlyStack == null) {
                    modelOnlyStack = new ConfigModelStack(configId, this);
                }
                modelOnlyStack.pushConfig(config);
                ++pushedCount;
            }

            boolean extendedStackLevel = false;
            if (fp.getSpec().hasTransitiveDeps()) {
                for (FeaturePackConfig fpDep : fp.getSpec().getTransitiveDeps()) {
                    extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                }
            }

            if(fp.getSpec().hasFeaturePackDeps()) {
                for(FeaturePackConfig fpDep : fp.getSpec().getFeaturePackDeps()) {
                    extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                    if(fpDep.isConfigModelExcluded(configId) || !fpDep.isInheritModelOnlyConfigs() && !fpDep.isConfigModelIncluded(configId)) {
                        continue;
                    }
                    modelOnlyStack = resolveModelOnlyConfig(fpDep, modelOnlyStack, configId);
                }
            }
            while(pushedCount > 0) {
                setOrigin(fp);
                processConfig(modelOnlyStack, popConfig(modelOnlyStack));
                --pushedCount;
            }
            if (extendedStackLevel) {
                fpConfigStack.popLevel();
            }
        } finally {
            setOrigin(prevOrigin);
        }
        return modelOnlyStack;
    }

    private void processConfig(ConfigModelStack configStack, ConfigModel config) throws ProvisioningException {
        this.configStack = configStack;
        configStack.overwriteProps(config.getProperties());
        configStack.overwriteConfigDeps(config.getConfigDeps());
        try {
            if(config.hasPackageDeps()) {
                if(currentOrigin == null) {
                    throw new ProvisioningDescriptionException(Errors.topConfigsCantDefinePackageDeps(config.getId()));
                }
                processPackageDeps(config, null);
            }
            processConfigItemContainer(config);
            this.configStack = null;
        } catch (ProvisioningException e) {
            throw new ProvisioningException(Errors.failedToResolveConfigSpec(config.getModel(), config.getName()), e);
        }
    }

    private void processConfigLayer(ConfigModelStack configStack, FeatureGroupSupport layer) throws ProvisioningException {
        this.configStack = configStack;
        try {
            if(layer.hasPackageDeps()) {
                processPackageDeps(layer, null);
            }
            processConfigItemContainer(layer);
            this.configStack = null;
        } catch (ProvisioningException e) {
            throw new ProvisioningException(Errors.failedToResolveConfigLayer(configStack.id.getModel(), layer.getName()), e);
        }
    }

    private ConfigModelStack getConfigStack(ConfigId id) throws ProvisioningException {
        if(id.getName() == null) {
            throw new IllegalStateException("Config model not associated with a name");
        }
        if(id.getModel() == null) {
            configStack = nameOnlyConfigs.get(id.getName());
            if(configStack == null) {
                configStack = new ConfigModelStack(id, this);
                nameOnlyConfigs = CollectionUtils.putLinked(nameOnlyConfigs, id.getName(), configStack);
            }
            return configStack;
        }

        Map<String, ConfigModelStack> namedConfigs = namedModelConfigs.get(id.getModel());
        if(namedConfigs == null) {
            configStack = new ConfigModelStack(id, this);
            namedConfigs = Collections.singletonMap(id.getName(), configStack);
            namedModelConfigs = CollectionUtils.putLinked(namedModelConfigs, id.getModel(), namedConfigs);
            return configStack;
        }

        configStack = namedConfigs.get(id.getName());
        if(configStack != null) {
            return configStack;
        }
        if (namedConfigs.size() == 1) {
            namedConfigs = new LinkedHashMap<>(namedConfigs);
            if (namedModelConfigs.size() == 1) {
                namedModelConfigs = new LinkedHashMap<>(namedModelConfigs);
            }
            namedModelConfigs.put(id.getModel(), namedConfigs);
        }
        configStack = new ConfigModelStack(id, this);
        namedConfigs.put(id.getName(), configStack);
        return configStack;
    }

    private void processFeatureGroup(FeatureGroupSupport includedFg)
            throws ProvisioningException {

        final boolean pushed = configStack.pushGroup(includedFg);

        final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
        try {
            final FeatureGroupSupport originalFg = getFeatureGroupSpec(includedFg.getName());
            if (originalFg.hasPackageDeps()) {
                processPackageDeps(originalFg, null);
            }
            if (!pushed) {
                return;
            }
            processConfigItemContainer(originalFg);
        } finally {
            currentOrigin = originalOrigin;
        }
        configStack.popGroup();

        if(includedFg.hasItems()) {
            processConfigItemContainer(includedFg);
        }
    }

    private FeaturePackRuntimeBuilder setOrigin(String origin) throws ProvisioningException {
        return origin == null ? currentOrigin : setOrigin(getOrigin(origin));
    }

    FeaturePackRuntimeBuilder setOrigin(FeaturePackRuntimeBuilder origin) {
        final FeaturePackRuntimeBuilder prevOrigin = this.currentOrigin;
        this.currentOrigin = origin;
        return prevOrigin;
    }

    FeaturePackRuntimeBuilder getOrigin(final String depName) throws ProvisioningException {
        if(Constants.THIS.equals(depName)) {
            if(thisOrigin == null) {
                throw new ProvisioningException("Feature-pack reference 'this' cannot be used in the current context.");
            }
            return thisOrigin;
        }
        final FeaturePackLocation fpl = currentOrigin == null ? config.getFeaturePackDep(depName).getLocation() : currentOrigin.getSpec().getFeaturePackDep(depName).getLocation();
        return layout.getFeaturePack(fpl.getProducer());
    }

    private FeaturePackRuntimeBuilder setThisOrigin(FeaturePackRuntimeBuilder origin) {
        final FeaturePackRuntimeBuilder prevOrigin = thisOrigin;
        thisOrigin = origin;
        return prevOrigin;
    }

    ResolvedFeatureGroupConfig resolveFeatureGroupConfig(ConfigModelStack configStack, FeatureGroupSupport fg) throws ProvisioningException {
        ProducerSpec fgOrigin = null;
        if(!(fg.isConfig() || fg.isLayer())) {
            final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
            getFeatureGroupSpec(fg.getName());
            fgOrigin = currentOrigin.producer;
            currentOrigin = originalOrigin;
        }
        final ResolvedFeatureGroupConfig resolvedFgc = new ResolvedFeatureGroupConfig(configStack, fg, fgOrigin);
        resolvedFgc.inheritFeatures = fg.isInheritFeatures();
        if(fg.hasExcludedSpecs()) {
            resolvedFgc.excludedSpecs = resolveSpecIds(resolvedFgc.excludedSpecs, fg.getExcludedSpecs());
        }
        if(fg.hasIncludedSpecs()) {
            resolvedFgc.includedSpecs = resolveSpecIds(resolvedFgc.includedSpecs, fg.getIncludedSpecs());
        }
        if(fg.hasExcludedFeatures()) {
            resolvedFgc.excludedFeatures = resolveExcludedIds(resolvedFgc.excludedFeatures, fg.getExcludedFeatures());
        }
        if(fg.hasIncludedFeatures()) {
            resolvedFgc.includedFeatures = resolveIncludedIds(resolvedFgc.includedFeatures, fg.getIncludedFeatures());
        }
        if(fg.hasExternalFeatureGroups()) {
            final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
            for (Map.Entry<String, FeatureGroup> entry : fg.getExternalFeatureGroups().entrySet()) {
                final FeatureGroup extFg = entry.getValue();
                setOrigin(entry.getKey());
                try {
                    if (extFg.hasExcludedSpecs()) {
                        resolvedFgc.excludedSpecs = resolveSpecIds(resolvedFgc.excludedSpecs, extFg.getExcludedSpecs());
                    }
                    if (extFg.hasIncludedSpecs()) {
                        resolvedFgc.includedSpecs = resolveSpecIds(resolvedFgc.includedSpecs, extFg.getIncludedSpecs());
                    }
                    if (extFg.hasExcludedFeatures()) {
                        resolvedFgc.excludedFeatures = resolveExcludedIds(resolvedFgc.excludedFeatures, extFg.getExcludedFeatures());
                    }
                    if (extFg.hasIncludedFeatures()) {
                        resolvedFgc.includedFeatures = resolveIncludedIds(resolvedFgc.includedFeatures, extFg.getIncludedFeatures());
                    }
                } finally {
                    setOrigin(originalOrigin);
                }
            }
        }
        return resolvedFgc;
    }

    void processIncludedFeatures(final ResolvedFeatureGroupConfig pushedFgConfig) throws ProvisioningException {
        if (pushedFgConfig.includedFeatures.isEmpty()) {
            return;
        }
        for (Map.Entry<ResolvedFeatureId, FeatureConfig> feature : pushedFgConfig.includedFeatures.entrySet()) {
            final FeatureConfig includedFc = feature.getValue();
            if (includedFc != null && includedFc.hasParams()) {
                final ResolvedFeatureId includedId = feature.getKey();
                if (pushedFgConfig.configStack.isFilteredOut(includedId.specId, includedId)) {
                    continue;
                }
                // make sure the included ID is in fact present on the feature group branch
                if (!pushedFgConfig.configStack.includes(includedId)) {
                    throw new ProvisioningException(Errors.featureNotInScope(includedId,
                            pushedFgConfig.fg.getId() == null ? "'anonymous'" : pushedFgConfig.fg.getId().toString(),
                            currentOrigin == null ? null : currentOrigin.producer.getLocation().getFPID()));
                }
                resolveFeature(pushedFgConfig.configStack, includedFc);
            }
        }
    }

    private Map<ResolvedFeatureId, FeatureConfig> resolveIncludedIds(Map<ResolvedFeatureId, FeatureConfig> includedFeatures, Map<FeatureId, FeatureConfig> features) throws ProvisioningException {
        for (Map.Entry<FeatureId, FeatureConfig> included : features.entrySet()) {
            final FeatureConfig fc = new FeatureConfig(included.getValue());
            final ResolvedFeatureSpec resolvedSpec = getFeatureSpec(fc.getSpecId().getName());
            if (parentFeature != null) {
                includedFeatures = CollectionUtils.put(includedFeatures, resolvedSpec.resolveIdFromForeignKey(parentFeature.id, fc.getParentRef(), fc.getParams()), fc);
            } else {
                includedFeatures = CollectionUtils.put(includedFeatures, resolvedSpec.resolveFeatureId(fc.getParams()), fc);
            }
        }
        return includedFeatures;
    }

    private Set<ResolvedFeatureId> resolveExcludedIds(Set<ResolvedFeatureId> resolvedIds, Map<FeatureId, String> features) throws ProvisioningException {
        for (Map.Entry<FeatureId, String> excluded : features.entrySet()) {
            final FeatureId excludedId = excluded.getKey();
            final ResolvedFeatureSpec resolvedSpec = getFeatureSpec(excludedId.getSpec().getName());
            if(parentFeature != null) {
                resolvedIds = CollectionUtils.add(resolvedIds, resolvedSpec.resolveIdFromForeignKey(parentFeature.id, excluded.getValue(), excludedId.getParams()));
            } else {
                resolvedIds = CollectionUtils.add(resolvedIds, resolvedSpec.resolveFeatureId(excludedId.getParams()));
            }
        }
        return resolvedIds;
    }

    private Set<ResolvedSpecId> resolveSpecIds(Set<ResolvedSpecId> resolvedIds, Set<SpecId> specs) throws ProvisioningException {
        for (SpecId specId : specs) {
            resolvedIds = CollectionUtils.add(resolvedIds, getFeatureSpec(specId.getName()).id);
        }
        return resolvedIds;
    }

    private void processConfigItemContainer(ConfigItemContainer ciContainer) throws ProvisioningException {
        if(!ciContainer.hasItems()) {
            return;
        }
        final FeaturePackRuntimeBuilder prevFpOrigin = ciContainer.isResetFeaturePackOrigin() ? setThisOrigin(currentOrigin) : null;
        for (ConfigItem item : ciContainer.getItems()) {
            final FeaturePackRuntimeBuilder originalFp = setOrigin(item.getOrigin());
            try {
                if (item.isGroup()) {
                    processFeatureGroup((FeatureGroup) item);
                } else {
                    resolveFeature(configStack, (FeatureConfig) item);
                }
            } catch (ProvisioningException e) {
                if (currentOrigin == null) {
                    throw e;
                }
                throw new ProvisioningException(
                        item.isGroup() ? Errors.failedToProcess(currentOrigin.producer.getLocation().getFPID(), ((FeatureGroup) item).getName())
                                : Errors.failedToProcess(currentOrigin.producer.getLocation().getFPID(), (FeatureConfig) item),
                        e);
            } finally {
                setOrigin(originalFp);
            }
        }
        if(prevFpOrigin != null) {
            setThisOrigin(prevFpOrigin);
        }
    }

    private void resolveFeature(ConfigModelStack configStack, FeatureConfig fc) throws ProvisioningException {
        final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
        final ResolvedFeatureSpec spec = getFeatureSpec(fc.getSpecId().getName(), true);
        final ResolvedFeature originalParent = parentFeature;
        try {
            final ResolvedFeatureId resolvedId = parentFeature == null ? spec.resolveFeatureId(fc.getParams())
                    : spec.resolveIdFromForeignKey(parentFeature.id, fc.getParentRef(), fc.getParams());
            if (configStack.isFilteredOut(spec.id, resolvedId)) {
                return;
            }

            parentFeature = resolveFeatureDepsAndRefs(configStack, spec, resolvedId,
                    spec.resolveNonIdParams(parentFeature == null ? null : parentFeature.id, fc.getParentRef(), fc.getParams()),
                    fc.getFeatureDeps());
            if (fc.hasUnsetParams()) {
                parentFeature.unsetAllParams(fc.getUnsetParams(), true);
            }
            if (fc.hasResetParams()) {
                parentFeature.resetAllParams(fc.getResetParams());
            }
        } finally {
            currentOrigin = originalOrigin;
        }
        processConfigItemContainer(fc);
        parentFeature = originalParent;
    }

    private ResolvedFeature resolveFeatureDepsAndRefs(ConfigModelStack configStack,
            final ResolvedFeatureSpec spec, final ResolvedFeatureId resolvedId, Map<String, Object> resolvedParams,
            Collection<FeatureDependencySpec> featureDeps)
            throws ProvisioningException {

        if(spec.xmlSpec.hasPackageDeps()) {
            processPackageDeps(spec.xmlSpec, null);
        }

        final ResolvedFeature resolvedFeature = configStack.includeFeature(resolvedId, spec,
                resolvedParams, resolveFeatureDeps(configStack, featureDeps, spec),
                Collections.emptySet(), Collections.emptySet());

        if(spec.xmlSpec.hasFeatureRefs()) {
            final ResolvedFeature myParent = parentFeature;
            parentFeature = resolvedFeature;
            for(FeatureReferenceSpec refSpec : spec.xmlSpec.getFeatureRefs()) {
                if(!refSpec.isInclude()) {
                    continue;
                }
                final FeaturePackRuntimeBuilder originalFp = setOrigin(refSpec.getOrigin());
                try {
                    final ResolvedFeatureSpec refResolvedSpec = getFeatureSpec(refSpec.getFeature().getName());
                    final List<ResolvedFeatureId> refIds = spec.resolveRefId(parentFeature, refSpec, refResolvedSpec);
                    if (!refIds.isEmpty()) {
                        for (ResolvedFeatureId refId : refIds) {
                            if (configStack.includes(refId) || configStack.isFilteredOut(refId.specId, refId)) {
                                continue;
                            }
                            resolveFeatureDepsAndRefs(configStack, refResolvedSpec, refId, Collections.emptyMap(), Collections.emptyList());
                        }
                    }
                } finally {
                    setOrigin(originalFp);
                }
            }
            parentFeature = myParent;
        }
        return resolvedFeature;
    }

    private Map<ResolvedFeatureId, FeatureDependencySpec> resolveFeatureDeps(ConfigModelStack configStack,
            Collection<FeatureDependencySpec> featureDeps, final ResolvedFeatureSpec spec)
            throws ProvisioningException {
        Map<ResolvedFeatureId, FeatureDependencySpec> resolvedDeps = spec.resolveFeatureDeps(this, featureDeps);
        if(!resolvedDeps.isEmpty()) {
            for(Map.Entry<ResolvedFeatureId, FeatureDependencySpec> dep : resolvedDeps.entrySet()) {
                if(!dep.getValue().isInclude()) {
                    continue;
                }
                final ResolvedFeatureId depId = dep.getKey();
                if(configStack.includes(depId) || configStack.isFilteredOut(depId.specId, depId)) {
                    continue;
                }
                final FeatureDependencySpec depSpec = dep.getValue();
                final FeaturePackRuntimeBuilder originalFp = setOrigin(depSpec.getOrigin());
                try {
                    resolveFeatureDepsAndRefs(configStack, getFeatureSpec(depId.getSpecId().getName()), depId, Collections.emptyMap(), Collections.emptyList());
                } finally {
                    setOrigin(originalFp);
                }
            }
        }
        return resolvedDeps;
    }

    private void resolvePackage(final String pkgName, PackageRuntime.Builder parent, int type) throws ProvisioningException {
        final int offset = resolvedPkgBranch.size();
        boolean resolved = false;
        try {
            currentOrigin.setVisited(true);
            if (resolved = resolvePackage(currentOrigin, pkgName, parent, type)) {
                if (offset == 0) {
                    for (int i = resolvedPkgBranch.size() - 1; i >= 0; --i) {
                        final PackageRuntime.Builder pkg = resolvedPkgBranch.get(i);
                        pkg.schedule();
                        pkg.clearFlag(PackageRuntime.ON_DEP_BRANCH);
                    }
                    resolvedPkgBranch.clear();
                }
                return;
            }
        } catch (UnsatisfiedPackageDependencyException e) {
            if(PackageDependencySpec.isOptional(type)) {
                return;
            }
            throw e;
        } finally {
            if (!resolved) {
                int i = resolvedPkgBranch.size() - offset;
                while (i > 0) {
                    resolvedPkgBranch.remove(offset + --i).clearFlag(PackageRuntime.ON_DEP_BRANCH);
                }
            }
            clearVisitedFPs();
            currentOrigin.setVisited(false);
        }
        throw new ProvisioningDescriptionException(Errors.packageNotFound(currentOrigin.producer.getLocation().getFPID(), pkgName));
    }

    private boolean resolvePackage(FeaturePackRuntimeBuilder origin, String name, PackageRuntime.Builder parent, int type) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if (origin != null) {
            if(origin.resolvePackage(name, this, parent, type)) {
                return true;
            }
            fpDeps = origin.getSpec();
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return false;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            final FeaturePackRuntimeBuilder fpDepBuilder = layout.getFeaturePack(fpDep.getLocation().getProducer());
            if (setVisited(fpDepBuilder) && resolvePackage(fpDepBuilder, name, parent, type)) {
                return true;
            }
        }
        return false;
    }

    boolean addToPkgDepBranch(PackageRuntime.Builder pkg) {
        if(pkg.setFlag(PackageRuntime.ON_DEP_BRANCH)) {
            resolvedPkgBranch.add(pkg);
            return true;
        }
        return false;
    }

    void processPackageDeps(final PackageDepsSpec pkgDeps, PackageRuntime.Builder parent) throws ProvisioningException {
        if (pkgDeps.hasLocalPackageDeps()) {
            for (PackageDependencySpec dep : pkgDeps.getLocalPackageDeps()) {
                if(fpConfigStack.isPackageExcluded(currentOrigin.producer, dep.getName())) {
                    if(!dep.isOptional()) {
                        throw new UnsatisfiedPackageDependencyException(currentOrigin.getFPID(), dep.getName());
                    }
                    continue;
                }
                if ((pkgDepMask & dep.getType()) > 0) {
                    resolvePackage(dep.getName(), parent, dep.getType());
                }
            }
        }
        if(!pkgDeps.hasExternalPackageDeps()) {
            return;
        }
        for (String origin : pkgDeps.getPackageOrigins()) {
            final FeaturePackRuntimeBuilder originalFp = setOrigin(origin);
            try {
                for (PackageDependencySpec dep : pkgDeps.getExternalPackageDeps(origin)) {
                    if (fpConfigStack.isPackageExcluded(currentOrigin.producer, dep.getName())) {
                        if (!dep.isOptional()) {
                            throw new UnsatisfiedPackageDependencyException(currentOrigin.getFPID(), dep.getName());
                        }
                        continue;
                    }
                    if ((pkgDepMask & dep.getType()) > 0) {
                        resolvePackage(dep.getName(), parent, dep.getType());
                    }
                }
            } finally {
                setOrigin(originalFp);
            }
        }
    }

    List<ProvisionedConfig> getResolvedConfigs() throws ProvisioningException {
        final int configsTotal = configsToBuild.size();
        if(configsTotal == 0) {
            return Collections.emptyList();
        }
        List<ProvisionedConfig> configList = new ArrayList<>(configsTotal);
        for(Map.Entry<ConfigId, ConfigModelStack> entry : configsToBuild.entrySet()) {
            final ConfigId id = entry.getKey();
            if(id.getName() == null || contains(configList, id)) {
                continue;
            }
            orderConfig(entry.getValue(), configList, Collections.emptySet());
        }
        return configList.size() > 0 ? Collections.unmodifiableList(configList) : configList;
    }

    private void orderConfig(ConfigModelStack config, List<ProvisionedConfig> configList, Set<ConfigId> scheduledIds) throws ProvisioningException {
        if(!config.hasConfigDeps()) {
            configList.add(ResolvedConfig.build(config));
            return;
        }
        if(!config.id.isAnonymous()) {
            scheduledIds = CollectionUtils.add(scheduledIds, config.id);
        }
        for(ConfigId depId : config.getConfigDeps().values()) {
            if(scheduledIds.contains(depId) || contains(configList, depId)) {
                continue;
            }

            if(depId.isModelOnly()) {
                final Map<String, ConfigModelStack> configs = namedModelConfigs.get(depId.getModel());
                if(configs == null) {
                    throw new ProvisioningDescriptionException("Config " + config.id + " has unsatisfied dependency on config " + depId);
                }
                for(ConfigModelStack dep : configs.values()) {
                    if(contains(configList, dep.id)) {
                        continue;
                    }
                    orderConfig(dep, configList, scheduledIds);
                }
            } else {
                final ConfigModelStack configStack;
                if (depId.getModel() == null) {
                    configStack = nameOnlyConfigs.get(depId.getName());
                } else {
                    final Map<String, ConfigModelStack> configs = namedModelConfigs.get(depId.getModel());
                    if(configs == null) {
                        throw new ProvisioningDescriptionException("Config " + config.id + " has unsatisfied dependency on config " + depId);
                    }
                    configStack = configs.get(depId.getName());
                }
                if(configStack == null) {
                    throw new ProvisioningDescriptionException("Config " + config.id + " has unsatisfied dependency on config " + depId);
                }
                if(contains(configList, configStack.id)) {
                    continue;
                }
                orderConfig(configStack, configList, scheduledIds);
            }
        }
        scheduledIds = CollectionUtils.remove(scheduledIds, config.id);
        configList.add(ResolvedConfig.build(config));
    }

    private boolean contains(List<ProvisionedConfig> configList, ConfigId depId) {
        int i = 0;
        while(i < configList.size()) {
            if(((ResolvedConfig)configList.get(i++)).id.equals(depId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * NOTE: this method will change the current origin to the origin of the group!
     */
    private FeatureGroup getFeatureGroupSpec(String name) throws ProvisioningException {
        final FeatureGroup fg = getFeatureGroupSpec(currentOrigin, name);
        clearVisitedFPs();
        if(fg == null) {
            throw new ProvisioningDescriptionException("Failed to locate feature group '" + name + "' in " + (currentOrigin == null ? "the provisioning configuration" : currentOrigin.producer + " and its dependencies"));
        }
        return fg;
    }

    private FeatureGroup getFeatureGroupSpec(FeaturePackRuntimeBuilder origin, String name) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if(origin != null) {
            if(origin.isVisited()) {
                return null;
            }
            final FeatureGroup fg = origin.getFeatureGroupSpec(name);
            if(fg != null) {
                currentOrigin = origin;
                return fg;
            }
            fpDeps = origin.getSpec();
            setVisited(origin);
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return null;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            final FeatureGroup fg = getFeatureGroupSpec(layout.getFeaturePack(fpDep.getLocation().getProducer()), name);
            if (fg != null) {
                return fg;
            }
        }
        return null;
    }

    private ResolvedFeatureSpec getFeatureSpec(String name) throws ProvisioningException {
        return getFeatureSpec(name, false);
    }

    private ResolvedFeatureSpec getFeatureSpec(String name, boolean switchOrigin) throws ProvisioningException {
        return getFeatureSpec(currentOrigin, name, switchOrigin);
    }

    ResolvedFeatureSpec getFeatureSpec(FeaturePackRuntimeBuilder origin, String name) throws ProvisioningException {
        return getFeatureSpec(origin, name, false);
    }

    private ResolvedFeatureSpec getFeatureSpec(FeaturePackRuntimeBuilder origin, String name, boolean switchOrigin) throws ProvisioningException {
        final ResolvedFeatureSpec resolvedSpec = findFeatureSpec(origin, name, switchOrigin);
        clearVisitedFPs();
        if(resolvedSpec == null) {
            if(origin == null) {
                throw new ProvisioningDescriptionException("Failed to locate feature spec '" + name + "' in the installed feature-packs.");
            }
            throw new ProvisioningDescriptionException("Failed to locate feature spec '" + name + "' in " + origin.producer + " and its dependencies.");
        }
        return resolvedSpec;
    }

    private ResolvedFeatureSpec findFeatureSpec(FeaturePackRuntimeBuilder origin, String name, boolean switchOrigin) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if (origin != null) {
            if(origin.isVisited()) {
                return null;
            }
            final ResolvedFeatureSpec fs = origin.getFeatureSpec(name);
            if (fs != null) {
                if (switchOrigin) {
                    currentOrigin = origin;
                }
                return fs;
            }
            fpDeps = origin.getSpec();
            setVisited(origin);
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return null;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            final ResolvedFeatureSpec fs = findFeatureSpec(layout.getFeaturePack(fpDep.getLocation().getProducer()), name, switchOrigin);
            if (fs != null) {
                return fs;
            }
        }
        return null;
    }

    private boolean setVisited(FeaturePackRuntimeBuilder fp) {
        if(fp.setVisited(true)) {
            visited.add(fp);
            return true;
        }
        return false;
    }

    private void clearVisitedFPs() {
        if (!visited.isEmpty()) {
            for (int i = 0; i < visited.size(); ++i) {
                visited.get(i).setVisited(false);
            }
            visited.clear();
        }
    }
}
