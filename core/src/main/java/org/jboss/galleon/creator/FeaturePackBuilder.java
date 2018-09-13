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

package org.jboss.galleon.creator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.tasks.FsTaskContext;
import org.jboss.galleon.creator.tasks.FsTaskList;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.ZipUtils;
import org.jboss.galleon.xml.ConfigLayerXmlWriter;
import org.jboss.galleon.xml.ConfigXmlWriter;
import org.jboss.galleon.xml.FeatureGroupXmlWriter;
import org.jboss.galleon.xml.FeaturePackXmlWriter;
import org.jboss.galleon.xml.FeatureSpecXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackBuilder {

    private final FeaturePackCreator creator;
    private final FeaturePackSpec.Builder fpBuilder = FeaturePackSpec.builder();
    private List<PackageBuilder> pkgs = Collections.emptyList();
    private Set<Class<?>> classes = Collections.emptySet();
    private Map<String, Set<String>> services = Collections.emptyMap();
    private String pluginFileName = "plugins.jar";
    private List<Path> plugins = Collections.emptyList();
    private Map<String, FeatureSpec> specs = Collections.emptyMap();
    private Map<String, FeatureGroup> featureGroups = Collections.emptyMap();
    private Map<ConfigId, ConfigModel> configs = Collections.emptyMap();
    private Map<ConfigId, ConfigLayerSpec> layers = Collections.emptyMap();
    private FsTaskList tasks;

    FeaturePackBuilder(FeaturePackCreator creator) {
        this.creator = creator;
    }

    public FeaturePackCreator getCreator() {
        return creator;
    }

    public FeaturePackBuilder setFPID(FeaturePackLocation.FPID fpid) {
        fpBuilder.setFPID(fpid);
        return this;
    }

    public FeaturePackBuilder setPatchFor(FeaturePackLocation.FPID fpid) {
        fpBuilder.setPatchFor(fpid);
        return this;
    }

    public FeaturePackBuilder setDefaultUniverse(String factory, String location) throws ProvisioningDescriptionException {
        fpBuilder.setDefaultUniverse(factory, location);
        return this;
    }

    public FeaturePackBuilder addUniverse(String name, String factory, String location) throws ProvisioningDescriptionException {
        fpBuilder.addUniverse(name, factory, location);
        return this;
    }

    public FeaturePackBuilder addDependency(String origin, FeaturePackConfig dep) throws ProvisioningDescriptionException {
        fpBuilder.addFeaturePackDep(origin, dep);
        return this;
    }

    public FeaturePackBuilder addDependency(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addDependency(FeaturePackConfig.forLocation(fpl));
    }

    public FeaturePackBuilder addDependency(FeaturePackConfig dep) throws ProvisioningDescriptionException {
        if(dep.isTransitive()) {
            fpBuilder.addFeaturePackDep(dep);
            return this;
        }
        return addDependency(null, dep);
    }

    public FeaturePackBuilder addDependency(String origin, FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return addDependency(origin, FeaturePackConfig.forLocation(fpl));
    }

    public FeaturePackBuilder addTransitiveDependency(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        fpBuilder.addTransitiveDep(fpl);
        return this;
    }

    public FeaturePackBuilder addPackage(PackageBuilder pkg) {
        pkgs = CollectionUtils.add(pkgs, pkg);
        return this;
    }

    public PackageBuilder newPackage(String name) {
        return newPackage(name, false);
    }

    public PackageBuilder newPackage(String name, boolean isDefault) {
        final PackageBuilder pkg = PackageBuilder.newInstance(this, name);
        if(isDefault) {
            pkg.setDefault();
        }
        addPackage(pkg);
        return pkg;
    }

    /**
     * @deprecated in favor of addFeatureSpec(FeatureSpec spec)
     * @param spec  feature spec
     * @return  this builder
     * @throws ProvisioningDescriptionException  in case of duplicate feature spec
     */
    public FeaturePackBuilder addSpec(FeatureSpec spec) throws ProvisioningDescriptionException {
        return addFeatureSpec(spec);
    }

    public FeaturePackBuilder addFeatureSpec(FeatureSpec spec) throws ProvisioningDescriptionException {
        if (specs.isEmpty()) {
            specs = new HashMap<>();
        }
        if (specs.put(spec.getName(), spec) != null) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": duplicate spec name " + spec.getName());
        }
        return this;
    }

    public FeaturePackBuilder addFeatureGroup(FeatureGroup featureGroup) throws ProvisioningDescriptionException {
        if (featureGroups.isEmpty()) {
            featureGroups = new HashMap<>();
        }
        if (featureGroups.put(featureGroup.getName(), featureGroup) != null) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": duplicate feature-group name " + featureGroup.getName());
        }
        return this;
    }

    public FeaturePackBuilder addConfig(ConfigModel config) throws ProvisioningDescriptionException {
        return addConfig(config, !config.getId().isModelOnly());
    }

    public FeaturePackBuilder addConfig(ConfigModel config, boolean asDefault) throws ProvisioningDescriptionException {
        final ConfigId id = config.getId();
        if(id.isAnonymous()) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": attempt to add an anonymous config");
        }
        if(asDefault && id.isModelOnly()) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": model-only config can not be added as the default one");
        }
        if (configs.isEmpty()) {
            configs = new HashMap<>();
        }
        if (configs.put(id, config) != null) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": duplicate config " + id);
        }
        if(asDefault) {
            fpBuilder.addConfig(ConfigModel.builder(id.getModel(), id.getName()).build());
        }
        return this;
    }

    public FeaturePackBuilder addConfigLayer(ConfigLayerSpec layer) throws ProvisioningDescriptionException {
        if(layers.isEmpty()) {
            layers = new HashMap<>();
        }
        if(layers.put(layer.getId(), layer) != null) {
            throw new ProvisioningDescriptionException("Feature-pack " + fpBuilder.getFPID() + ": duplicate layer " + layer.getId());
        }
        return this;
    }

    public FeaturePackBuilder setPluginFileName(String pluginFileName) {
        this.pluginFileName = pluginFileName;
        return this;
    }

    public FeaturePackBuilder addClassToPlugin(Class<?> cls) {
        if(classes.contains(cls)) {
            return this;
        }
        classes = CollectionUtils.add(classes, cls);
        return this;
    }

    public FeaturePackBuilder addPlugin(Path file) {
        if(plugins.contains(file)) {
            return this;
        }
        plugins = CollectionUtils.add(plugins, file);
        return this;
    }

    public FeaturePackBuilder addPlugin(Class<? extends InstallPlugin> pluginCls) {
        return addService(InstallPlugin.class, pluginCls);
    }

    public FeaturePackBuilder addService(Class<?> serviceInterface, Class<?> serviceImpl) {
        final String serviceName = serviceInterface.getName();
        Set<String> implSet = services.get(serviceName);
        if(implSet == null) {
            services = CollectionUtils.put(services, serviceName, Collections.singleton(serviceImpl.getName()));
        } else {
            if(implSet.contains(serviceImpl.getName())) {
                return this;
            }
            if(implSet.size() == 1) {
                implSet = new HashSet<>(implSet);
                implSet.add(serviceImpl.getName());
                if(services.size() == 1) {
                    services = Collections.singletonMap(serviceName, implSet);
                } else {
                    services.put(serviceName, implSet);
                }
            } else {
                implSet.add(serviceImpl.getName());
            }
        }
        addClassToPlugin(serviceImpl);
        return this;
    }

    public FeaturePackBuilder writeResources(String relativePath, String content) throws ProvisioningDescriptionException {
        if(tasks == null) {
            tasks = FsTaskList.newList();
        }
        tasks.write(content, relativePath, false);
        return this;
    }

    void build() throws ProvisioningException {
        final FeaturePackLocation fps = fpBuilder.getFPID().getLocation();
        if(fps == null) {
            throw new ProvisioningDescriptionException("Feature-pack location has not been set");
        }
        if(fps.getProducerName() == null) {
            throw new ProvisioningDescriptionException("Feature-pack producer has not been set");
        }
        if(fps.getChannelName() == null) {
            throw new ProvisioningDescriptionException("Feature-pack channel has not been set");
        }
        if(fps.getBuild() == null) {
            throw new ProvisioningDescriptionException("Feature-pack build number has not been set");
        }
        final Path fpWorkDir = LayoutUtils.getFeaturePackDir(creator.getWorkDir(), fps.getFPID(), false);
        final FeaturePackSpec fpSpec;
        try {
            ensureDir(fpWorkDir);
            for (PackageBuilder pkg : pkgs) {
                final PackageSpec pkgDescr = pkg.build(fpWorkDir);
                if(pkg.isDefault()) {
                    fpBuilder.addDefaultPackage(pkgDescr.getName());
                }
            }

            if(!specs.isEmpty()) {
                final Path featuresDir = fpWorkDir.resolve(Constants.FEATURES);
                final FeatureSpecXmlWriter specWriter = FeatureSpecXmlWriter.getInstance();
                for(FeatureSpec spec : specs.values()) {
                    final Path featureDir = featuresDir.resolve(spec.getName());
                    ensureDir(featureDir);
                    specWriter.write(spec, featureDir.resolve(Constants.SPEC_XML));
                }
            }

            if(!featureGroups.isEmpty()) {
                final Path fgsDir = fpWorkDir.resolve(Constants.FEATURE_GROUPS);
                ensureDir(fgsDir);
                final FeatureGroupXmlWriter fgWriter = FeatureGroupXmlWriter.getInstance();
                for(FeatureGroup fg : featureGroups.values()) {
                    fgWriter.write(fg, fgsDir.resolve(fg.getName() + ".xml"));
                }
            }

            if(!classes.isEmpty() || !plugins.isEmpty()) {
                addPlugins(fpWorkDir);
            }

            if(!layers.isEmpty()) {
                for(Map.Entry<ConfigId, ConfigLayerSpec> entry : layers.entrySet()) {
                    final ConfigId id = entry.getKey();
                    final Path xml = LayoutUtils.getLayerSpecXml(fpWorkDir, id.getModel(), id.getName(), false);
                    if (Files.exists(xml)) {
                        throw new ProvisioningException("Failed to create feature-pack: " + xml + " already exists");
                    }
                    ConfigLayerXmlWriter.getInstance().write(entry.getValue(), xml);
                }
            }

            if(!configs.isEmpty()) {
                for(ConfigModel config : configs.values()) {
                    final Path modelXml = LayoutUtils.getConfigXml(fpWorkDir, config.getId(), false);
                    if (Files.exists(modelXml)) {
                        throw new ProvisioningException("Failed to create feature-pack: " + modelXml + " already exists");
                    }
                    ConfigXmlWriter.getInstance().write(config, modelXml);
                }
            }

            fpSpec = fpBuilder.build();
            final FeaturePackXmlWriter writer = FeaturePackXmlWriter.getInstance();
            writer.write(fpSpec, fpWorkDir.resolve(Constants.FEATURE_PACK_XML));

            if(tasks != null && !tasks.isEmpty()) {
                tasks.execute(FsTaskContext.builder().setTargetRoot(fpWorkDir.resolve(Constants.RESOURCES)).build());
            }
            creator.install(fps.getFPID(), fpWorkDir);
        } catch(ProvisioningDescriptionException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IoUtils.recursiveDelete(fpWorkDir);
        }
    }

    private void addPlugins(Path fpDir) throws IOException {
        final Path tmpDir = IoUtils.createRandomTmpDir();
        try {
            byte[] bytes = new byte[65536];
            for(Class<?> cls : classes) {
                Path p = tmpDir;
                final String[] parts = cls.getName().split("\\.");
                int i = 0;
                while(i < parts.length - 1) {
                    p = p.resolve(parts[i++]);
                }
                p = p.resolve(parts[i] + ".class");
                Files.createDirectories(p.getParent());

                final InputStream is = cls.getClassLoader().getResourceAsStream(tmpDir.relativize(p).toString());
                if(is == null) {
                    throw new IOException("Failed to locate " + tmpDir.relativize(p));
                }
                try (OutputStream os = Files.newOutputStream(p)) {
                    int rc;
                    while ((rc = is.read(bytes)) != -1) {
                        os.write(bytes, 0, rc);
                    }
                    os.flush();
                } finally {
                    try {
                        is.close();
                    } catch(IOException e) {
                    }
                }
            }

            if(!services.isEmpty()) {
                final Path servicesDir = tmpDir.resolve("META-INF").resolve("services");
                Files.createDirectories(servicesDir);
                for(Map.Entry<String, Set<String>> entry : services.entrySet()) {
                    final Path service = servicesDir.resolve(entry.getKey());
                    try(BufferedWriter writer = Files.newBufferedWriter(service)) {
                        for(String impl : entry.getValue()) {
                            writer.write(impl);
                            writer.newLine();
                        }
                    }
                }
            }

            final Path pluginsDir = fpDir.resolve(Constants.PLUGINS);
            ensureDir(pluginsDir);
            ZipUtils.zip(tmpDir, pluginsDir.resolve(pluginFileName));
            if(!plugins.isEmpty()) {
                for(Path plugin : plugins) {
                    Files.copy(plugin, pluginsDir.resolve(plugin.getFileName()));
                }
            }
        } finally {
            IoUtils.recursiveDelete(tmpDir);
        }
    }

    private void ensureDir(Path dir) throws IOException {
        if(!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if(!Files.isDirectory(dir)) {
            throw new IllegalStateException(dir + " is not a directory.");
        }
    }
}
