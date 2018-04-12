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
package org.jboss.galleon.repomanager;

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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.repomanager.fs.FsTaskContext;
import org.jboss.galleon.repomanager.fs.FsTaskList;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.ZipUtils;
import org.jboss.galleon.xml.FeatureGroupXmlWriter;
import org.jboss.galleon.xml.FeaturePackXmlWriter;
import org.jboss.galleon.xml.FeatureSpecXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackBuilder {

    public static FeaturePackBuilder newInstance() {
        return newInstance(null);
    }

    public static FeaturePackBuilder newInstance(FeaturePackInstaller installer) {
        return new FeaturePackBuilder(installer);
    }

    private final FeaturePackInstaller installer;
    private final FeaturePackSpec.Builder fpBuilder = FeaturePackSpec.builder();
    private List<PackageBuilder> pkgs = Collections.emptyList();
    private Set<Class<?>> classes = Collections.emptySet();
    private Map<String, Set<String>> services = Collections.emptyMap();
    private String pluginFileName = "plugins.jar";
    private List<Path> plugins = Collections.emptyList();
    private Map<String, FeatureSpec> specs = Collections.emptyMap();
    private Map<String, FeatureGroup> featureGroups = Collections.emptyMap();
    private FsTaskList tasks;


    protected FeaturePackBuilder(FeaturePackInstaller repo) {
        this.installer = repo;
    }

    public FeaturePackInstaller getInstaller() {
        return installer;
    }

    public FeaturePackBuilder setGav(ArtifactCoords.Gav gav) {
        fpBuilder.setGav(gav);
        return this;
    }

    public FeaturePackBuilder addDependency(String origin, FeaturePackConfig dep) throws ProvisioningDescriptionException {
        fpBuilder.addFeaturePackDep(origin, dep);
        return this;
    }

    public FeaturePackBuilder addDependency(FeaturePackConfig dep) throws ProvisioningDescriptionException {
        return addDependency(null, dep);
    }

    public FeaturePackBuilder addDependency(ArtifactCoords.Gav gav) throws ProvisioningDescriptionException {
        return addDependency(FeaturePackConfig.forGav(gav));
    }

    public FeaturePackBuilder addDependency(String origin, ArtifactCoords.Gav gav) throws ProvisioningDescriptionException {
        return addDependency(origin, FeaturePackConfig.forGav(gav));
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

    public FeaturePackBuilder addSpec(FeatureSpec spec) throws ProvisioningDescriptionException {
        if(specs.isEmpty()) {
            specs = Collections.singletonMap(spec.getName(), spec);
        } else {
            if(specs.containsKey(spec.getName())) {
                throw new ProvisioningDescriptionException("Duplicate spec name " + spec.getName() + " for " + fpBuilder.getGav());
            }
            if(specs.size() == 1) {
                specs = new HashMap<>(specs);
            }
            specs.put(spec.getName(), spec);
        }
        return this;
    }

    public FeaturePackBuilder addFeatureGroup(FeatureGroup featureGroup) throws ProvisioningDescriptionException {
        if(featureGroups.isEmpty()) {
            featureGroups = Collections.singletonMap(featureGroup.getName(), featureGroup);
        } else {
            if(featureGroups.containsKey(featureGroup.getName())) {
                throw new ProvisioningDescriptionException("Duplicate feature-group name " + featureGroup.getName() + " for " + fpBuilder.getGav());
            }
            if(featureGroups.size() == 1) {
                featureGroups = new HashMap<>(featureGroups);
            }
            featureGroups.put(featureGroup.getName(), featureGroup);
        }
        return this;
    }

    public FeaturePackBuilder addConfig(ConfigModel config) throws ProvisioningDescriptionException {
        fpBuilder.addConfig(config);
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
        switch(classes.size()) {
            case 0:
                classes = Collections.singleton(cls);
                break;
            case 1:
                classes = new HashSet<>(classes);
            default:
                classes.add(cls);
        }
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
            switch(services.size()) {
                case 0:
                    services = Collections.singletonMap(serviceName, Collections.singleton(serviceImpl.getName()));
                    break;
                case 1:
                    services = new HashMap<>(services);
                default:
                    services.put(serviceName, Collections.singleton(serviceImpl.getName()));
            }
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

    public FeaturePackSpec build(ArtifactRepositoryManager manager) throws ProvisioningDescriptionException {
        final Path fpWorkDir = IoUtils.createRandomTmpDir();
        final FeaturePackSpec fpSpec;
        try {
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
            fpSpec = fpBuilder.build();
            final FeaturePackXmlWriter writer = FeaturePackXmlWriter.getInstance();
            writer.write(fpSpec, fpWorkDir.resolve(Constants.FEATURE_PACK_XML));

            if(tasks != null && !tasks.isEmpty()) {
                tasks.execute(FsTaskContext.builder().setTargetRoot(fpWorkDir.resolve(Constants.RESOURCES)).build());
            }
            manager.install(fpSpec.getGav().toArtifactCoords(), fpWorkDir);
            return fpSpec;
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
