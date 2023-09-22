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
package org.jboss.galleon.caller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.CoreVersion;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.api.GalleonFeaturePackLayout;
import org.jboss.galleon.api.GalleonProvisioningLayout;
import org.jboss.galleon.api.GalleonProvisioningRuntime;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.xml.ProvisioningXmlParser;
import org.jboss.galleon.xml.ProvisioningXmlWriter;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilder;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayersBuilderItf;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.core.builder.LocalFP;
import org.jboss.galleon.core.builder.ProvisioningContext;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.spec.FeaturePackPlugin;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.xml.ProvisionedStateXmlParser;
import org.jboss.galleon.xml.XmlParsers;

public class ProvisioningContextImpl implements ProvisioningContext {

    private ProvisioningManager provisionManager;
    private ProvisioningLayoutFactory factory;
    private final URLClassLoader loader;
    private final Path home;
    private final MessageWriter msgWriter;
    private final boolean logTime;
    private final boolean recordState;
    private final UniverseResolver universeResolver;
    private final Map<String, ProgressTracker<?>> progressTrackers;
    private final Map<FeaturePackLocation.FPID, LocalFP> locals;

    ProvisioningContextImpl(URLClassLoader loader, Path home,
            MessageWriter msgWriter,
            boolean logTime,
            boolean recordState,
            UniverseResolver universeResolver,
            Map<String, ProgressTracker<?>> progressTrackers,
            Map<FeaturePackLocation.FPID, LocalFP> locals) throws ProvisioningException {
        this.loader = loader;
        this.home = home;
        this.msgWriter = msgWriter;
        this.logTime = logTime;
        this.recordState = recordState;
        this.universeResolver = universeResolver;
        this.progressTrackers = progressTrackers;
        this.locals = locals;
    }

    @Override
    public GalleonProvisioningConfig getConfig(GalleonProvisioningConfig config) throws ProvisioningException {
        return ProvisioningConfig.toConfig(ProvisioningConfig.toConfig(config));
    }

    @Override
    public void provision(GalleonProvisioningConfig config, List<Path> customConfigs, Map<String, String> options) throws ProvisioningException {
//        if (noHome) {
//            throw new ProvisioningException("No installation set, can't provision.");
//        }
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            getManager().provision(ProvisioningConfig.toConfig(config, customConfigs), options);
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    @Override
    public void provision(Path config, Map<String, String> options) throws ProvisioningException {
//        if (noHome) {
//            throw new ProvisioningException("No installation set, can't provision.");
//        }
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            getManager().provision(config, options);
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    @Override
    public String getCoreVersion() {
        return CoreVersion.getVersion();
    }

    @Override
    public void storeProvisioningConfig(GalleonProvisioningConfig config, Path file) throws XMLStreamException, IOException, ProvisioningException {
        try (FileWriter writer = new FileWriter(file.toFile())) {
            ProvisioningXmlWriter.getInstance().write(ProvisioningConfig.toConfig(config), writer);
        }
    }

    @Override
    public GalleonProvisioningRuntime getProvisioningRuntime(GalleonProvisioningConfig config) throws ProvisioningException {
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            return getManager().getRuntime(ProvisioningConfig.toConfig(config));
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    @Override
    public UniverseResolver getUniverseResolver() {
        return universeResolver;
    }

    @Override
    public void close() {
        if (provisionManager != null) {
            provisionManager.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    @Override
    public GalleonProvisioningConfig parseProvisioningFile(Path provisioning) throws ProvisioningException {
        ProvisioningConfig c = ProvisioningXmlParser.parse(provisioning);
        if (c == null) {
            return null;
        }
        return ProvisioningConfig.toConfig(c);
    }

    @Override
    public List<GalleonFeaturePackLayout> getOrderedFeaturePackLayouts(GalleonProvisioningConfig config) throws ProvisioningException {
        List<GalleonFeaturePackLayout> lst = new ArrayList<>();
        try (ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(ProvisioningConfig.toConfig(config))) {
            lst.addAll(layout.getOrderedFeaturePacks());
            return lst;
        }
    }

    @Override
    public Set<String> getOrderedFeaturePackPluginLocations(GalleonProvisioningConfig config) throws ProvisioningException {
        Set<String> lst = new HashSet<>();
        try (ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(ProvisioningConfig.toConfig(config))) {
            for (FeaturePackLayout fp : layout.getOrderedFeaturePacks()) {
                for (FeaturePackPlugin plugin : fp.getSpec().getPlugins().values()) {
                    lst.add(plugin.getLocation());
                }
            }
        }
        return lst;
    }

    @Override
    public GalleonConfigurationWithLayersBuilderItf buildConfigurationBuilder(GalleonConfigurationWithLayers config) {
        if (config instanceof ConfigModel) {
            ConfigModel pconfig = (ConfigModel) config;
            return ConfigModel.builder(pconfig);
        } else {
            return GalleonConfigurationWithLayersBuilder.builder(config);
        }
    }

    @Override
    public List<String> getInstalledPacks(Path dir) throws ProvisioningException {
        final Collection<ProvisionedFeaturePack> featurePacks = ProvisionedStateXmlParser.parse(
                PathsUtils.getProvisionedStateXml(dir)).getFeaturePacks();

        return featurePacks.stream().map(fp -> fp.getFPID().getProducer().getName()).collect(Collectors.toList());
    }

    @Override
    public GalleonProvisioningConfig loadProvisioningConfig(InputStream is) throws ProvisioningException, XMLStreamException {
        InputStreamReader reader = new InputStreamReader(is);
        final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
        XmlParsers.parse(reader, builder);
        return ProvisioningConfig.toConfig(builder.build());
    }

    @Override
    public FsDiff getFsDiff() throws ProvisioningException {
        return getManager().getFsDiff();
    }

    @Override
    public void install(FeaturePackLocation loc) throws ProvisioningException {
        getManager().install(loc);
    }

    @Override
    public void install(GalleonFeaturePackConfig config) throws ProvisioningException {
        getManager().install(ProvisioningConfig.toFeaturePackConfig(config));
    }

    @Override
    public void uninstall(FeaturePackLocation.FPID loc) throws ProvisioningException {
        getManager().uninstall(loc);
    }

    @Override
    public boolean hasOrderedFeaturePacksConfig(GalleonProvisioningConfig config, ConfigId cfg) throws ProvisioningException {
        try (ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(ProvisioningConfig.toConfig(config))) {
            for (FeaturePackLayout fp : layout.getOrderedFeaturePacks()) {
                try {
                    LayoutUtils.getConfigXml(fp.getDir(), cfg, true);
                    return true;
                } catch (ProvisioningDescriptionException e) {
                }
            }
        }
        return false;
    }

    @Override
    public GalleonProvisioningLayout newProvisioningLayout(GalleonProvisioningConfig config) throws ProvisioningException {
        ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(ProvisioningConfig.toConfig(config));
        return new GalleonProvisioningLayoutImpl(layout);
    }

    @Override
    public GalleonProvisioningLayout newProvisioningLayout(Path file, boolean install) throws ProvisioningException {
        ProvisioningLayout<FeaturePackLayout> layout = getLayoutFactory().newConfigLayout(file, install);
        return new GalleonProvisioningLayoutImpl(layout);
    }
    private ProvisioningManager getManager() throws ProvisioningException {
        if (provisionManager == null) {
            ProvisioningManager.Builder builder = ProvisioningManager.builder()
                    .setInstallationHome(home)
                    .setMessageWriter(msgWriter)
                    .setLogTime(logTime)
                    .setRecordState(recordState);
            if (universeResolver != null) {
                builder.setUniverseResolver(universeResolver);
            }
            provisionManager = builder.build();
            for (Map.Entry<String, ProgressTracker<?>> entry : progressTrackers.entrySet()) {
                provisionManager.getLayoutFactory().setProgressTracker(entry.getKey(), entry.getValue());
            }
            for (LocalFP fp : locals.values()) {
                provisionManager.getLayoutFactory().addLocal(fp.getPath(), fp.isInstallInUniverse());
            }
        }
        return provisionManager;
    }

    private ProvisioningLayoutFactory getLayoutFactory() throws ProvisioningException {
        if (factory == null) {
            factory = ProvisioningLayoutFactory.getInstance();
            for (LocalFP fp : locals.values()) {
                factory.addLocal(fp.getPath(), fp.isInstallInUniverse());
            }
        }
        return factory;
    }
}
