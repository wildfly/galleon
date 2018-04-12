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
package org.jboss.galleon.cli.model.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.MavenArtifactRepositoryManager;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class State {

    interface Action {

        void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException;

        void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException;
    }

    private ProvisioningConfig config;
    private FeatureContainer container;

    private String path;
    private ProvisioningConfig.Builder builder;
    private final FeaturePackProvisioning fpProvisioning = new FeaturePackProvisioning();
    private final ConfigProvisioning configProvisioning = new ConfigProvisioning();
    private final Deque<Action> stack = new ArrayDeque<>();

    public State(PmSession pmSession) throws ProvisioningException, IOException {
        builder = ProvisioningConfig.builder();
        ProvisioningManager manager = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).build();
        init(pmSession, manager);
    }

    public State(PmSession pmSession, Path installation) throws ProvisioningException, IOException {
        ProvisioningManager manager;
        ProvisioningConfig conf;
        if (Files.isRegularFile(installation)) {
            manager = ProvisioningManager.builder()
                    .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).build();
            conf = manager.readProvisioningConfig(installation);
            builder = conf.getBuilder();
        } else {
            manager = ProvisioningManager.builder()
                    .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).
                    setInstallationHome(installation).
                    build();
            if (manager.getProvisionedState() == null) {
                throw new ProvisioningException(installation + " is not an installation dir");
            }
            conf = manager.getProvisioningConfig();
            builder = conf.getBuilder();
        }

        Set<Gav> dependencies = new HashSet<>();
        for (FeaturePackConfig cf : conf.getFeaturePackDeps()) {
            dependencies.add(cf.getGav());
        }
        init(pmSession, manager);
        Map<String, FeatureContainer> fullDependencies = new HashMap<>();
        buildDependencies(pmSession, dependencies, fullDependencies);
        container.setFullDependencies(fullDependencies);
    }

    private void init(PmSession pmSession, ProvisioningManager manager) throws ProvisioningException, IOException {
        config = builder.build();
        ProvisioningRuntime runtime = manager.getRuntime(config, null, Collections.emptyMap());
        container = FeatureContainers.fromProvisioningRuntime(pmSession, manager, runtime);
        container.setEdit(true);
        path = "" + PathParser.PATH_SEPARATOR;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasActions() {
        return !stack.isEmpty();
    }

    public ProvisioningConfig getConfig() {
        return config;
    }

    public FeatureContainer getContainer() {
        return container;
    }

    public void addDependency(PmSession pmSession, String name, ArtifactCoords.Gav gav, boolean inheritConfigs, boolean inheritPackages) throws
            ProvisioningException, IOException {
        Action action = fpProvisioning.addDependency(pmSession, name, gav, inheritConfigs, inheritPackages);
        config = pushState(action, pmSession);
    }

    public void removeDependency(PmSession pmSession, ArtifactCoords.Gav gav) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeDependency(gav);
        config = pushState(action, pmSession);
    }

    public void includeConfiguration(PmSession pmSession, Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.includeConfiguration(cf);
        config = pushState(action, pmSession);
    }

    public void removeIncludedConfiguration(PmSession pmSession, Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeIncludedConfiguration(cf);
        config = pushState(action, pmSession);
    }

    public void excludeConfiguration(PmSession pmSession, Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.excludeConfiguration(cf);
        config = pushState(action, pmSession);
    }

    public void removeExcludedConfiguration(PmSession pmSession, Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeExcludedConfiguration(cf);
        config = pushState(action, pmSession);
    }

    public void resetConfiguration(PmSession pmSession, ConfigInfo configuration) throws ProvisioningException, IOException {
        ConfigId id = new ConfigId(configuration.getModel(), configuration.getName());
        Action action = configProvisioning.resetConfiguration(id);
        config = pushState(action, pmSession);
    }

    public void includePackage(PmSession pmSession, String pkg, FeaturePackConfig cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.includePackage(pkg, cf);
        config = pushState(action, pmSession);
    }

    public void removeIncludedPackage(PmSession pmSession, Map<FeaturePackConfig, String> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeIncludedPackage(cf);
        config = pushState(action, pmSession);
    }

    public void excludePackage(PmSession pmSession, String pkg, FeaturePackConfig cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.excludePackage(pkg, cf);
        config = pushState(action, pmSession);
    }

    public void removeExcludedPackage(PmSession pmSession, Map<FeaturePackConfig, String> cf) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeExcludedPackage(cf);
        config = pushState(action, pmSession);
    }

    public void addFeature(PmSession pmSession, FeatureSpecInfo spec, ConfigInfo configuration,
            Map<String, String> options) throws ProvisioningException, IOException {
        ConfigId id = new ConfigId(configuration.getModel(), configuration.getName());
        Action action = configProvisioning.addFeature(id, spec, options);
        config = pushState(action, pmSession);
    }

    public void removeFeature(PmSession pmSession, ConfigInfo ci, FeatureInfo fi)
            throws ProvisioningException, IOException {
        ConfigId id = new ConfigId(ci.getModel(), ci.getName());
        Action action = configProvisioning.removeFeature(id, fi);
        config = pushState(action, pmSession);
    }

    public void export(Path file) throws Exception {
        ProvisioningXmlWriter.getInstance().write(config, file);
    }

    public void pop(PmSession pmSession) throws IOException, ProvisioningException {
        Action action = stack.peek();
        if (action != null) {
            config = popState(action, pmSession);
        }
    }

    private ProvisioningConfig pushState(Action action, PmSession pmSession) throws IOException, ProvisioningException {
        action.doAction(config, builder);
        try {
            ProvisioningConfig newConfig = buildNewConfig(pmSession);
            stack.push(action);
            return newConfig;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                action.undoAction(builder);
            } catch (Exception ex2) {
                ex.addSuppressed(ex2);
            }
            throw ex;
        }
    }

    private ProvisioningConfig popState(Action action, PmSession pmSession) throws IOException, ProvisioningException {
        action.undoAction(builder);
        try {
            ProvisioningConfig newConfig = buildNewConfig(pmSession);
            stack.remove();
            return newConfig;
        } catch (IOException | ProvisioningException ex) {
            action.doAction(config, builder);
            throw ex;
        }

    }

    private ProvisioningConfig buildNewConfig(PmSession pmSession) throws ProvisioningException, IOException {
        ProvisioningConfig tmp = builder.build();
        ProvisioningManager manager = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).build();
        ProvisioningRuntime runtime = manager.getRuntime(tmp, null, Collections.emptyMap());
        Set<Gav> dependencies = new HashSet<>();
        for (FeaturePackConfig cf : tmp.getFeaturePackDeps()) {
            dependencies.add(cf.getGav());
        }
        FeatureContainer tmpContainer = FeatureContainers.fromProvisioningRuntime(pmSession, manager, runtime);
        // Need to have in sync the current with the full.
        // If fullConainer creation is a failure, the container will be not updated.
        Set<Gav> newDeps = new HashSet<>();
        for (FeaturePackConfig cf : tmp.getFeaturePackDeps()) {
            newDeps.add(cf.getGav());
        }
        Map<String, FeatureContainer> tmpDeps = new HashMap<>();
        tmpDeps.putAll(container.getFullDependencies());
        buildDependencies(pmSession, dependencies, tmpDeps);
        container = tmpContainer;
        container.setEdit(true);
        container.setFullDependencies(tmpDeps);
        return tmp;
    }

    private void buildDependencies(PmSession session, Set<Gav> dependencies, Map<String, FeatureContainer> deps) throws ProvisioningException, IOException {
        ProvisioningManager manager = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).build();
        for (Gav gav : dependencies) {
            String orig = Identity.buildOrigin(gav);
            if (!deps.containsKey(orig)) {
                // Need to add individual featurepack.
                deps.put(orig, FeatureContainers.fromFeaturePackGav(session, manager, gav, null));
            }
        }
        // Remove feature-packs that would have been removed.
        Iterator<FeatureContainer> it = deps.values().iterator();
        while (it.hasNext()) {
            FeatureContainer fc = it.next();
            if (!dependencies.contains(fc.getGav())) {
                it.remove();
            }
        }
    }

}
