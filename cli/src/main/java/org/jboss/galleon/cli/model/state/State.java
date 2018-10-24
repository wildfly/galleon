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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
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
import org.jboss.galleon.runtime.FeaturePackRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.xml.ProvisioningXmlParser;
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
    private final Provisioning provisioning = new Provisioning();
    private final ConfigProvisioning configProvisioning = new ConfigProvisioning();
    private final Deque<Action> stack = new ArrayDeque<>();
    private ProvisioningRuntime runtime;
    private String name;

    public State(PmSession pmSession) throws ProvisioningException, IOException {
        init(pmSession);
    }

    public void close() {
        runtime.close();
    }

    public State(PmSession pmSession, Path installation) throws ProvisioningException, IOException {
        ProvisioningConfig conf;
        if (Files.isRegularFile(installation)) {
            conf = ProvisioningXmlParser.parse(installation);
        } else {
            PathsUtils.assertInstallationDir(installation);
            conf = ProvisioningXmlParser.parse(PathsUtils.getProvisioningXml(installation));
        }

        Set<FeaturePackLocation.FPID> dependencies = new HashSet<>();
        for (FeaturePackConfig cf : conf.getFeaturePackDeps()) {
            dependencies.add(cf.getLocation().getFPID());
        }
        builder = conf.getBuilder();
        config = buildNewConfig(pmSession);
        path = "" + PathParser.PATH_SEPARATOR;
        name = installation.getFileName().toString();
    }

    public String getName() {
        return name;
    }

    public ProvisioningRuntime getRuntime() {
        return runtime;
    }

    private void init(PmSession pmSession) throws ProvisioningException, IOException {
        builder = ProvisioningConfig.builder();
        config = builder.build();
        runtime = ProvisioningRuntimeBuilder.newInstance(pmSession.getMessageWriter(false))
                .initLayout(pmSession.getLayoutFactory(), config)
                .setEncoding(ProvisioningManager.Builder.ENCODING)
                .build();
        container = FeatureContainers.fromProvisioningRuntime(pmSession, runtime);
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

    public void addDependency(PmSession pmSession, String name, FeaturePackLocation fpl, boolean inheritConfigs, boolean inheritPackages) throws
            ProvisioningException, IOException {
        Action action = fpProvisioning.addDependency(pmSession, name, fpl, inheritConfigs, inheritPackages);
        config = pushState(action, pmSession);
    }

    public void removeDependency(PmSession pmSession, FeaturePackLocation fpl) throws ProvisioningException, IOException {
        Action action = fpProvisioning.removeDependency(fpl);
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

    public void addUniverse(PmSession pmSession, String name, String factory, String location) throws ProvisioningException, IOException {
        Action action = provisioning.addUniverse(name, factory, location);
        config = pushState(action, pmSession);
    }

    public void removeUniverse(PmSession pmSession, String name) throws ProvisioningException, IOException {
        Action action = provisioning.removeUniverse(name);
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
            //ex.printStackTrace();
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
        if (runtime != null) {
            runtime.close();
        }
        runtime = ProvisioningRuntimeBuilder.newInstance(pmSession.getMessageWriter(false))
                .initLayout(pmSession.getLayoutFactory(), tmp)
                .setEncoding(ProvisioningManager.Builder.ENCODING)
                .build();
        Set<FeaturePackLocation.FPID> dependencies = new HashSet<>();
        for (FeaturePackRuntime rt : runtime.getFeaturePacks()) {
            dependencies.add(rt.getFPID());
        }
        FeatureContainer tmpContainer = FeatureContainers.fromProvisioningRuntime(pmSession, runtime);
        // Need to have in sync the current with the full.
        // If fullConainer creation is a failure, the container will be not updated.
        Map<String, FeatureContainer> tmpDeps = new HashMap<>();
        if (container != null) {
            tmpDeps.putAll(container.getFullDependencies());
        }
        buildDependencies(pmSession, dependencies, tmpDeps);
        container = tmpContainer;
        container.setEdit(true);
        container.setFullDependencies(tmpDeps);
        return tmp;
    }

    private void buildDependencies(PmSession session, Set<FeaturePackLocation.FPID> dependencies, Map<String, FeatureContainer> deps) throws ProvisioningException, IOException {
        for (FeaturePackLocation.FPID fpid : dependencies) {
            String orig = Identity.buildOrigin(fpid.getProducer());
            if (!deps.containsKey(orig)) {
                // Need to add individual featurepack.
                deps.put(orig, FeatureContainers.fromFeaturePackId(session, fpid, null));
            }
        }
        // Remove feature-packs that would have been removed.
        Iterator<FeatureContainer> it = deps.values().iterator();
        while (it.hasNext()) {
            FeatureContainer fc = it.next();
            if (!dependencies.contains(fc.getFPID())) {
                it.remove();
            }
        }
    }

}
