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
package org.jboss.galleon.runtime;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageDepsSpec;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.CollectionUtils;


/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningRuntimeBuilder {

    public static ProvisioningRuntimeBuilder newInstance() {
        return newInstance(DefaultMessageWriter.getDefaultInstance());
    }

    public static ProvisioningRuntimeBuilder newInstance(final MessageWriter messageWriter) {
        return new ProvisioningRuntimeBuilder(messageWriter);
    }

    private static void mkdirs(final Path path) throws ProvisioningException {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(path));
        }
    }

    final long startTime;
    String encoding;
    String operation;
    UniverseResolver universeResolver;
    ProvisioningConfig config;
    private Map<ProducerSpec, FPID> uninstallFps = Collections.emptyMap();
    Path installDir;
    final Path workDir;
    final Path layoutDir;
    Path pluginsDir = null;
    Map<String, String> pluginOptions = Collections.emptyMap();
    private final MessageWriter messageWriter;

    private final Map<ProducerSpec, FeaturePackRuntimeBuilder> fpRtBuilders = new HashMap<>();
    private List<FeaturePackRuntimeBuilder> fpRtBuildersOrdered = new ArrayList<>();

    List<ConfigModelStack> anonymousConfigs = Collections.emptyList();
    Map<String, ConfigModelStack> nameOnlyConfigs = Collections.emptyMap();
    Map<String, ConfigModelStack> modelOnlyConfigs = Collections.emptyMap();
    Map<String, Map<String, ConfigModelStack>> namedModelConfigs = Collections.emptyMap();

    // this is a stack of model only configs that are resolved and merged after all
    // the named model configs have been resolved. This is done to:
    // 1) avoid resolving model only configs that are not going to get merged;
    // 2) to avoid adding package dependencies of the model only configs that are not merged.
    private List<ConfigModel> modelOnlyConfigSpecs = Collections.emptyList();
    private List<FPID> modelOnlyFPIDs = Collections.emptyList();

    private FeaturePackRuntimeBuilder thisOrigin;
    private FeaturePackRuntimeBuilder currentOrigin;
    private ConfigModelStack configStack;

    private FpStack fpConfigStack;

    private ResolvedFeature parentFeature;

    private ProvisioningRuntimeBuilder(final MessageWriter messageWriter) {
        startTime = System.currentTimeMillis();
        workDir = IoUtils.createRandomTmpDir();
        layoutDir = workDir.resolve("layout");
        this.messageWriter = messageWriter;
    }

    public ProvisioningRuntimeBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public ProvisioningRuntimeBuilder setUniverseResolver(UniverseResolver universeResolver) {
        this.universeResolver = universeResolver;
        return this;
    }

    public ProvisioningRuntimeBuilder setConfig(ProvisioningConfig config) {
        this.config = config;
        return this;
    }

    public ProvisioningRuntimeBuilder setInstallDir(Path installDir) {
        this.installDir = installDir;
        return this;
    }

    public ProvisioningRuntimeBuilder uninstall(FeaturePackLocation.FPID fpid) {
        uninstallFps = CollectionUtils.put(uninstallFps, fpid.getProducer(), fpid);
        return this;
    }

    public ProvisioningRuntimeBuilder setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public ProvisioningRuntime build() throws ProvisioningException {
        try {
            return doBuild();
        } catch(ProvisioningException | RuntimeException | Error e) {
            IoUtils.recursiveDelete(workDir);
            throw e;
        }
    }

    private ProvisioningRuntime doBuild() throws ProvisioningException {

        if(!uninstallFps.isEmpty()) {
            Map<ProducerSpec, FPID> depsOfUninstalled = Collections.emptyMap();
            for(ProducerSpec uninstallFp : uninstallFps.keySet()) {
                if(!config.hasFeaturePackDep(uninstallFp)) {
                    throw new ProvisioningException(Errors.unknownFeaturePack(uninstallFp.getLocation().getFPID()));
                }
                final FeaturePackRuntimeBuilder fp = getOrLoadFpBuilder(uninstallFp.getLocation().getFPID());
                depsOfUninstalled = FpVersionsResolver.resolveDeps(this, fp, depsOfUninstalled);
            }
            if(!depsOfUninstalled.isEmpty()) {
                Map<ProducerSpec, FPID> depsOfRemaining = Collections.emptyMap();
                for (FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                    if (fpConfig.getLocation().getBuild() == null) {
                        continue;
                    }
                    final FPID uninstallGav = uninstallFps.get(fpConfig.getLocation().getProducer());
                    if (uninstallGav != null) {
                        if (!uninstallGav.equals(fpConfig.getLocation().getFPID())) {
                            throw new ProvisioningException(Errors.unknownFeaturePack(fpConfig.getLocation().getFPID()));
                        }
                        continue;
                    }
                    if (depsOfRemaining != null) {
                        depsOfRemaining = FpVersionsResolver.resolveDeps(this, getOrLoadFpBuilder(fpConfig.getLocation().getFPID()), depsOfRemaining);
                    }
                }
                if (!depsOfRemaining.isEmpty()) {
                    if (depsOfUninstalled.size() == 1) {
                        final FPID depOfRemaining = depsOfRemaining.get(depsOfUninstalled.keySet().iterator().next());
                        if (depOfRemaining != null) {
                            depsOfUninstalled = Collections.emptyMap();
                        }
                    } else {
                        for (ProducerSpec depOfRemaining : depsOfRemaining.keySet()) {
                            if (depsOfUninstalled.remove(depOfRemaining) != null) {
                                if (depsOfUninstalled.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // TODO copy the configs
            final ProvisioningConfig.Builder configBuilder = ProvisioningConfig.builder();
            for (FeaturePackConfig fpConfig : config.getFeaturePackDeps()) {
                final ProducerSpec producer = fpConfig.getLocation().getProducer();
                if(uninstallFps.containsKey(producer)) {
                    continue;
                }
                if(fpConfig.getLocation().getBuild() == null && depsOfUninstalled.containsKey(producer)) {
                    continue;
                }
                final String origin = config.originOf(producer);
                configBuilder.addFeaturePackDep(origin, fpConfig);
            }
            config = configBuilder.build();

            if(!config.hasFeaturePackDeps()) {
                emptyHomeDir();
                IoUtils.recursiveDelete(workDir);
                return null;
            }
        }

        FpVersionsResolver.resolveFpVersions(this);

        fpConfigStack = new FpStack(config);

        // the configs are processed in the reverse order to correctly implement config overwrites

        List<ConfigModelStack> fpConfigResolvers = Collections.emptyList();
        for(int i = config.getDefinedConfigs().size() - 1; i >= 0; --i) {
            final ConfigModel config = this.config.getDefinedConfigs().get(i);
            if(fpConfigStack.isFilteredOut(config.getId(), true)) {
                continue;
            }
            configStack = getConfigStack(config.getId());
            configStack.pushConfig(config);
            fpConfigResolvers = CollectionUtils.add(fpConfigResolvers, configStack);
        }

        final Collection<FeaturePackConfig> fpConfigs = config.getFeaturePackDeps();
        boolean extendedStackLevel = false;
        for (FeaturePackConfig fpConfig : fpConfigs) {
            extendedStackLevel |= fpConfigStack.push(fpConfig, extendedStackLevel);
        }
        while(fpConfigStack.hasNext()) {
            processFpConfig(fpConfigStack.next());
        }

        if(extendedStackLevel) {
            fpConfigStack.popLevel();
        }

        for(int i = fpConfigResolvers.size() - 1; i>= 0; --i) {
            final ConfigModelStack configResolver = fpConfigResolvers.get(i);
            final ConfigModel config = configResolver.popConfig();
            if(config.getId().isModelOnly()) {
                recordModelOnlyConfig(null, config);
                continue;
            }
            processConfig(configResolver, config);
        }

        mergeModelOnlyConfigs();

        return new ProvisioningRuntime(this, messageWriter);
    }

    Map<ProducerSpec, FeaturePackRuntime> getFpRuntimes(ProvisioningRuntime runtime) throws ProvisioningException {
        if(fpRtBuildersOrdered.isEmpty()) {
            return Collections.emptyMap();
        }
        if(fpRtBuildersOrdered.size() == 1) {
            final FeaturePackRuntimeBuilder builder = fpRtBuildersOrdered.get(0);
            copyResources(builder);
            return Collections.singletonMap(builder.fpid.getProducer(), builder.build(runtime));
        }
        final Map<ProducerSpec, FeaturePackRuntime> fpRuntimes = new LinkedHashMap<>(fpRtBuildersOrdered.size());
        for (FeaturePackRuntimeBuilder builder : fpRtBuildersOrdered) {
            copyResources(builder);
            fpRuntimes.put(builder.fpid.getProducer(), builder.build(runtime));
        }
        return Collections.unmodifiableMap(fpRuntimes);
    }

    private void mergeModelOnlyConfigs() throws ProvisioningException {
        if(!modelOnlyConfigSpecs.isEmpty()) {
            for(int i = 0; i < modelOnlyConfigSpecs.size(); ++i) {
                final ConfigModel modelOnlySpec = modelOnlyConfigSpecs.get(i);
                if(!namedModelConfigs.containsKey(modelOnlySpec.getModel())) {
                    continue;
                }
                fpConfigStack.activateConfigStack(i);
                final FPID fpid = modelOnlyFPIDs.get(i);
                thisOrigin = fpid == null ? null : getFpBuilder(fpid.getProducer());
                setOrigin(thisOrigin);
                if(processConfig(getConfigStack(modelOnlySpec.getId()), modelOnlySpec) && currentOrigin != null && !currentOrigin.ordered) {
                    orderFpRtBuilder(currentOrigin);
                }
            }
        }
        if(modelOnlyConfigs.isEmpty()) {
            return;
        }
        final Iterator<Map.Entry<String, ConfigModelStack>> i = modelOnlyConfigs.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry<String, ConfigModelStack> entry = i.next();
            final Map<String, ConfigModelStack> targetConfigs = namedModelConfigs.get(entry.getKey());
            if (targetConfigs != null) {
                for (Map.Entry<String, ConfigModelStack> targetConfig : targetConfigs.entrySet()) {
                    targetConfig.getValue().merge(entry.getValue());
                }
            }
        }
        modelOnlyConfigs = Collections.emptyMap();
    }

    private void processFpConfig(FeaturePackConfig fpConfig) throws ProvisioningException {
        thisOrigin = getFpBuilder(fpConfig.getLocation().getProducer());
        final FeaturePackRuntimeBuilder parentFp = setOrigin(thisOrigin);

        try {
            List<ConfigModelStack> fpConfigStacks = Collections.emptyList();
            for (int i = fpConfig.getDefinedConfigs().size() - 1; i >= 0; --i) {
                final ConfigModel config = fpConfig.getDefinedConfigs().get(i);
                if (fpConfigStack.isFilteredOut(config.getId(), true)) {
                    continue;
                }
                configStack = getConfigStack(config.getId());
                configStack.pushConfig(config);
                fpConfigStacks = CollectionUtils.add(fpConfigStacks, configStack);
            }

            List<ConfigModelStack> specConfigStacks = Collections.emptyList();
            for (int i = currentOrigin.spec.getDefinedConfigs().size() - 1; i >= 0; --i) {
                final ConfigModel config = currentOrigin.spec.getDefinedConfigs().get(i);
                if (fpConfigStack.isFilteredOut(config.getId(), false)) {
                    continue;
                }
                configStack = getConfigStack(config.getId());
                configStack.pushConfig(config);
                specConfigStacks = CollectionUtils.add(specConfigStacks, configStack);
            }

            configStack = null;

            boolean extendedStackLevel = false;
            if (currentOrigin.spec.hasFeaturePackDeps()) {
                final Collection<FeaturePackConfig> fpDeps = currentOrigin.spec.getFeaturePackDeps();
                for (FeaturePackConfig fpDep : fpDeps) {
                    extendedStackLevel |= fpConfigStack.push(fpDep, extendedStackLevel);
                }
                if (extendedStackLevel) {
                    while (fpConfigStack.hasNext()) {
                        processFpConfig(fpConfigStack.next());
                    }
                }
            }

            boolean contributed = false;

            for (int i = specConfigStacks.size() - 1; i >= 0; --i) {
                final ConfigModelStack configStack = specConfigStacks.get(i);
                final ConfigModel config = configStack.popConfig();
                if (config.getId().isModelOnly()) {
                    recordModelOnlyConfig(fpConfig.getLocation().getFPID(), config);
                    continue;
                }
                contributed |= processConfig(configStack, config);
            }

            if (fpConfig.isInheritPackages()) {
                for (String packageName : currentOrigin.spec.getDefaultPackageNames()) {
                    if (fpConfigStack.isPackageFilteredOut(currentOrigin.fpid.getProducer(), packageName, false)) {
                        continue;
                    }
                    resolvePackage(packageName);
                    contributed = true;
                }
            }
            if (fpConfig.hasIncludedPackages()) {
                for (PackageConfig pkgConfig : fpConfig.getIncludedPackages()) {
                    if (fpConfigStack.isPackageFilteredOut(currentOrigin.fpid.getProducer(), pkgConfig.getName(), true)) {
                        continue;
                    }
                    resolvePackage(pkgConfig.getName());
                    contributed = true;
                }
            }

            for (int i = fpConfigStacks.size() - 1; i >= 0; --i) {
                final ConfigModelStack configStack = fpConfigStacks.get(i);
                final ConfigModel config = configStack.popConfig();
                if (config.getId().isModelOnly()) {
                    recordModelOnlyConfig(fpConfig.getLocation().getFPID(), config);
                    continue;
                }
                contributed |= processConfig(configStack, config);
            }

            if (extendedStackLevel) {
                fpConfigStack.popLevel();
            }

            if (!currentOrigin.ordered && contributed) {
                orderFpRtBuilder(currentOrigin);
            }
        } finally {
            this.thisOrigin = parentFp;
            setOrigin(parentFp);
        }
    }

    private void recordModelOnlyConfig(FPID fpid, ConfigModel config) {
        modelOnlyConfigSpecs = CollectionUtils.add(modelOnlyConfigSpecs, config);
        modelOnlyFPIDs = CollectionUtils.add(modelOnlyFPIDs, fpid);
        fpConfigStack.recordStack();
    }

    private boolean processConfig(ConfigModelStack configStack, ConfigModel config) throws ProvisioningException {
        this.configStack = configStack;
        configStack.overwriteProps(config.getProperties());
        configStack.overwriteConfigDeps(config.getConfigDeps());
        try {
            if(config.hasPackageDeps()) {
                processPackageDeps(config);
            }
            processConfigItemContainer(config);
            this.configStack = null;
            return true; // the config may be empty but it may tigger model-only merge into it
        } catch (ProvisioningException e) {
            throw new ProvisioningException(Errors.failedToResolveConfigSpec(config.getModel(), config.getName()), e);
        }
    }

    private ConfigModelStack getConfigStack(ConfigId id) throws ProvisioningException {
        ConfigModelStack configStack;
        if(id.getModel() == null) {
            if(id.getName() == null) {
                configStack = new ConfigModelStack(id, this);
                anonymousConfigs = CollectionUtils.add(anonymousConfigs, configStack);
                return configStack;
            }
            configStack = nameOnlyConfigs.get(id.getName());
            if(configStack == null) {
                configStack = new ConfigModelStack(id, this);
                nameOnlyConfigs = CollectionUtils.putLinked(nameOnlyConfigs, id.getName(), configStack);
            }
            return configStack;
        }
        if(id.getName() == null) {
            configStack = modelOnlyConfigs.get(id.getModel());
            if(configStack == null) {
                configStack = new ConfigModelStack(id, this);
                modelOnlyConfigs = CollectionUtils.putLinked(modelOnlyConfigs, id.getModel(), configStack);
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

    private boolean processFeatureGroup(FeatureGroupSupport includedFg)
            throws ProvisioningException {

        final boolean pushed = configStack.pushGroup(includedFg);

        final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
        boolean resolvedFeatures = false;
        try {
            final FeatureGroupSupport originalFg = getFeatureGroupSpec(includedFg.getName());
            if (originalFg.hasPackageDeps()) {
                processPackageDeps(originalFg);
            }
            if (!pushed) {
                return false;
            }
            resolvedFeatures = processConfigItemContainer(originalFg);
        } finally {
            currentOrigin = originalOrigin;
        }
        resolvedFeatures |= configStack.popGroup();

        if(includedFg.hasItems()) {
            resolvedFeatures |= processConfigItemContainer(includedFg);
        }
        return resolvedFeatures;
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
        final FeaturePackLocation fpl = currentOrigin == null ? config.getFeaturePackDep(depName).getLocation() : currentOrigin.spec.getFeaturePackDep(depName).getLocation();
        return getFpBuilder(fpl.getProducer());
    }

    private FeaturePackRuntimeBuilder setThisOrigin(FeaturePackRuntimeBuilder origin) {
        final FeaturePackRuntimeBuilder prevOrigin = thisOrigin;
        thisOrigin = origin;
        return prevOrigin;
    }

    ResolvedFeatureGroupConfig resolveFeatureGroupConfig(FeatureGroupSupport fg) throws ProvisioningException {
        FPID fgOrigin = null;
        if(!fg.isConfig()) {
            final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
            getFeatureGroupSpec(fg.getName());
            fgOrigin = currentOrigin.fpid;
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

    boolean processIncludedFeatures(final ResolvedFeatureGroupConfig pushedFgConfig)
            throws ProvisioningException {
        if (pushedFgConfig.includedFeatures.isEmpty()) {
            return false;
        }
        boolean resolvedFeatures = false;
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
                            currentOrigin == null ? null : currentOrigin.fpid));
                }
                resolvedFeatures |= resolveFeature(pushedFgConfig.configStack, includedFc);
            }
        }
        return resolvedFeatures;
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

    private boolean processConfigItemContainer(ConfigItemContainer ciContainer) throws ProvisioningException {
        if(!ciContainer.hasItems()) {
            return false;
        }
        boolean resolvedFeatures = false;
        final FeaturePackRuntimeBuilder prevFpOrigin = ciContainer.isResetFeaturePackOrigin() ? setThisOrigin(currentOrigin) : null;
        for (ConfigItem item : ciContainer.getItems()) {
            final FeaturePackRuntimeBuilder originalFp = setOrigin(item.getOrigin());
            try {
                if (item.isGroup()) {
                    final FeatureGroup nestedFg = (FeatureGroup) item;
                    resolvedFeatures |= processFeatureGroup(nestedFg);
                } else {
                    resolvedFeatures |= resolveFeature(configStack, (FeatureConfig) item);
                }
            } catch (ProvisioningException e) {
                if (currentOrigin == null) {
                    throw e;
                }
                throw new ProvisioningException(
                        item.isGroup() ? Errors.failedToProcess(currentOrigin.fpid, ((FeatureGroup) item).getName())
                                : Errors.failedToProcess(currentOrigin.fpid, (FeatureConfig) item),
                        e);
            } finally {
                setOrigin(originalFp);
            }
        }
        if(prevFpOrigin != null) {
            setThisOrigin(prevFpOrigin);
        }
        return resolvedFeatures;
    }

    private boolean resolveFeature(ConfigModelStack configStack, FeatureConfig fc) throws ProvisioningException {
        final FeaturePackRuntimeBuilder originalOrigin = currentOrigin;
        final ResolvedFeatureSpec spec = getFeatureSpec(fc.getSpecId().getName(), true);
        final ResolvedFeature originalParent = parentFeature;
        try {
            final ResolvedFeatureId resolvedId = parentFeature == null ? spec.resolveFeatureId(fc.getParams())
                    : spec.resolveIdFromForeignKey(parentFeature.id, fc.getParentRef(), fc.getParams());
            if (configStack.isFilteredOut(spec.id, resolvedId)) {
                return false;
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

            if (!currentOrigin.ordered) {
                orderFpRtBuilder(currentOrigin);
            }
        } finally {
            currentOrigin = originalOrigin;
        }
        processConfigItemContainer(fc);
        parentFeature = originalParent;
        return true;
    }

    private ResolvedFeature resolveFeatureDepsAndRefs(ConfigModelStack configStack,
            final ResolvedFeatureSpec spec, final ResolvedFeatureId resolvedId, Map<String, Object> resolvedParams,
            Collection<FeatureDependencySpec> featureDeps)
            throws ProvisioningException {

        if(spec.xmlSpec.hasPackageDeps()) {
            processPackageDeps(spec.xmlSpec);
        }

        final ResolvedFeature resolvedFeature = configStack.includeFeature(resolvedId, spec, resolvedParams, resolveFeatureDeps(configStack, featureDeps, spec));

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

    FeaturePackRuntimeBuilder getFpBuilder(ProducerSpec producer) throws ProvisioningDescriptionException {
        return getFpBuilder(producer, true);
    }

    FeaturePackRuntimeBuilder getFpBuilder(ProducerSpec producer, boolean failIfNotFound) throws ProvisioningDescriptionException {
        final FeaturePackRuntimeBuilder fp = fpRtBuilders.get(producer);
        if(fp == null && failIfNotFound) {
            throw new ProvisioningDescriptionException(Errors.unknownFeaturePack(producer.getLocation().getFPID()));
        }
        return fp;
    }

    FeaturePackRuntimeBuilder getOrLoadFpBuilder(FPID fpid) throws ProvisioningException {
        FeaturePackRuntimeBuilder fp = getFpBuilder(fpid.getProducer(), false);
        if(fp == null) {
            final Path fpDir = LayoutUtils.getFeaturePackDir(layoutDir, fpid, false);
            mkdirs(fpDir);
            fp = new FeaturePackRuntimeBuilder(universeResolver, fpid, fpDir);
            fpRtBuilders.put(fpid.getProducer(), fp);
        }
        return fp;
    }

    private void resolvePackage(final String pkgName) throws ProvisioningException {
        if(resolvePackage(currentOrigin, pkgName, Collections.emptySet(), false)) {
            return;
        }
        throw new ProvisioningDescriptionException(Errors.packageNotFound(currentOrigin.fpid, pkgName));
    }

    private boolean resolvePackage(FeaturePackRuntimeBuilder origin, String name, Set<ProducerSpec> visitedChannels, boolean switchOrigin) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if (origin != null) {
            if(origin.resolvePackage(name, this)) {
                return true;
            }
            fpDeps = origin.spec;
            visitedChannels = CollectionUtils.add(visitedChannels, origin.fpid.getProducer());
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return false;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            if (visitedChannels.contains(fpDep.getLocation().getProducer())) {
                continue;
            }
            if(resolvePackage(getOrLoadFpBuilder(fpDep.getLocation().getFPID()), name, visitedChannels, switchOrigin)) {
                return true;
            }
        }
        return false;
    }

    void processPackageDeps(final PackageDepsSpec pkgDeps) throws ProvisioningException {
        if (pkgDeps.hasLocalPackageDeps()) {
            for (PackageDependencySpec dep : pkgDeps.getLocalPackageDeps()) {
                if(fpConfigStack.isPackageExcluded(currentOrigin.fpid.getProducer(), dep.getName())) {
                    if(!dep.isOptional()) {
                        throw new ProvisioningDescriptionException(Errors.unsatisfiedPackageDependency(currentOrigin.fpid, dep.getName()));
                    }
                    continue;
                }
                try {
                    resolvePackage(dep.getName());
                } catch(ProvisioningDescriptionException e) {
                    if(dep.isOptional()) {
                        continue;
                    } else {
                        throw e;
                    }
                }
            }
        }
        if(!pkgDeps.hasExternalPackageDeps()) {
            return;
        }
        for (String origin : pkgDeps.getPackageOrigins()) {
            final FeaturePackRuntimeBuilder originalFp = setOrigin(origin);
            try {
                for (PackageDependencySpec pkgDep : pkgDeps.getExternalPackageDeps(origin)) {
                    if (fpConfigStack.isPackageExcluded(currentOrigin.fpid.getProducer(), pkgDep.getName())) {
                        if (!pkgDep.isOptional()) {
                            throw new ProvisioningDescriptionException(
                                    Errors.unsatisfiedPackageDependency(currentOrigin.fpid, pkgDep.getName()));
                        }
                        continue;
                    }
                    try {
                        resolvePackage(pkgDep.getName());
                    } catch (ProvisioningDescriptionException e) {
                        if (pkgDep.isOptional()) {
                            continue;
                        } else {
                            throw e;
                        }
                    }
                }
            } finally {
                setOrigin(originalFp);
            }
        }
    }

    void orderFpRtBuilder(final FeaturePackRuntimeBuilder fpRtBuilder) {
        fpRtBuildersOrdered.add(fpRtBuilder);
        fpRtBuilder.ordered = true;
    }

    List<ProvisionedConfig> getResolvedConfigs() throws ProvisioningException {

        final int configsTotal = anonymousConfigs.size() + nameOnlyConfigs.size() + namedModelConfigs.size();
        if(configsTotal == 0) {
            return Collections.emptyList();
        }

        List<ProvisionedConfig> configList = new ArrayList<>(configsTotal);
        if(!anonymousConfigs.isEmpty()) {
            for (ConfigModelStack config : anonymousConfigs) {
                orderConfig(config, configList, Collections.emptySet());
            }
        }
        if(!nameOnlyConfigs.isEmpty()) {
            for(ConfigModelStack config : nameOnlyConfigs.values()) {
                if(contains(configList, config.id)) {
                    continue;
                }
                orderConfig(config, configList, Collections.emptySet());
            }
        }
        if(!namedModelConfigs.isEmpty()) {
            for(Map.Entry<String, Map<String, ConfigModelStack>> entry : namedModelConfigs.entrySet()) {
                for(ConfigModelStack config : entry.getValue().values()) {
                    if(contains(configList, config.id)) {
                        continue;
                    }
                    orderConfig(config, configList, Collections.emptySet());
                }
            }
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

    private void copyResources(FeaturePackRuntimeBuilder fpRtBuilder) throws ProvisioningException {
        // resources should be copied last overriding the dependency resources
        final Path fpResources = fpRtBuilder.dir.resolve(Constants.RESOURCES);
        if(Files.exists(fpResources)) {
            try {
                IoUtils.copy(fpResources, workDir.resolve(Constants.RESOURCES));
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(fpResources, workDir.resolve(Constants.RESOURCES)), e);
            }
        }

        final Path fpPlugins = fpRtBuilder.dir.resolve(Constants.PLUGINS);
        if(Files.exists(fpPlugins)) {
            if(pluginsDir == null) {
                pluginsDir = workDir.resolve(Constants.PLUGINS);
            }
            try {
                IoUtils.copy(fpPlugins, pluginsDir);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(fpPlugins, workDir.resolve(Constants.PLUGINS)), e);
            }
        }
    }

    public ProvisioningRuntimeBuilder setOption(String name, String param) {
        pluginOptions = CollectionUtils.put(pluginOptions, name, param);
        return this;
    }

    public ProvisioningRuntimeBuilder addOptions(Map<String, String> options) {
        this.pluginOptions = CollectionUtils.putAll(this.pluginOptions, options);
        return this;
    }

    private void emptyHomeDir() throws ProvisioningException {
        if(!Files.exists(installDir)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(installDir)) {
            for (Path p : stream) {
                IoUtils.recursiveDelete(p);
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readDirectory(installDir));
        }
    }

    /**
     * NOTE: this method will change the current origin to the origin of the group!
     */
    private FeatureGroup getFeatureGroupSpec(String name) throws ProvisioningException {
        final FeatureGroup fg = getFeatureGroupSpec(currentOrigin, name, Collections.emptySet());
        if(fg == null) {
            throw new ProvisioningDescriptionException("Failed to locate feature group '" + name + "' in " + (currentOrigin == null ? "the provisioning configuration" : currentOrigin.fpid + " and its dependencies"));
        }
        return fg;
    }

    private FeatureGroup getFeatureGroupSpec(FeaturePackRuntimeBuilder origin, String name, Set<ProducerSpec> visitedProducers) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if(origin != null) {
            final FeatureGroup fg = origin.getFeatureGroupSpec(name);
            if(fg != null) {
                currentOrigin = origin;
                return fg;
            }
            fpDeps = origin.spec;
            visitedProducers = CollectionUtils.add(visitedProducers, origin.fpid.getProducer());
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return null;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            if (visitedProducers.contains(fpDep.getLocation().getProducer())) {
                continue;
            }
            final FeatureGroup fg = getFeatureGroupSpec(getOrLoadFpBuilder(fpDep.getLocation().getFPID()), name, visitedProducers);
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
        final ResolvedFeatureSpec resolvedSpec = getFeatureSpec(origin, name, Collections.emptySet(), switchOrigin);
        if(resolvedSpec == null) {
            if(origin == null) {
                throw new ProvisioningDescriptionException("Failed to locate feature spec '" + name + "' in the installed feature-packs.");
            }
            throw new ProvisioningDescriptionException("Failed to locate feature spec '" + name + "' in " + origin.fpid + " and its dependencies.");
        }
        return resolvedSpec;
    }

    private ResolvedFeatureSpec getFeatureSpec(FeaturePackRuntimeBuilder origin, String name, Set<ProducerSpec> visitedProducers, boolean switchOrigin) throws ProvisioningException {
        final FeaturePackDepsConfig fpDeps;
        if (origin != null) {
            final ResolvedFeatureSpec fs = origin.getFeatureSpec(name);
            if (fs != null) {
                if (switchOrigin) {
                    currentOrigin = origin;
                }
                return fs;
            }
            fpDeps = origin.spec;
            visitedProducers = CollectionUtils.add(visitedProducers, origin.fpid.getProducer());
        } else {
            fpDeps = config;
        }

        if (!fpDeps.hasFeaturePackDeps()) {
            return null;
        }

        for (FeaturePackConfig fpDep : fpDeps.getFeaturePackDeps()) {
            final FeaturePackLocation fps = fpDep.getLocation();
            if (visitedProducers.contains(fps.getProducer())) {
                continue;
            }
            final ResolvedFeatureSpec fs = getFeatureSpec(getOrLoadFpBuilder(fps.getFPID()), name, visitedProducers, switchOrigin);
            if (fs != null) {
                return fs;
            }
        }
        return null;
    }
}
