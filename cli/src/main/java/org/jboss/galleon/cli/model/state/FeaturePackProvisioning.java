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
package org.jboss.galleon.cli.model.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
class FeaturePackProvisioning {

    private class AddDependencyAction implements State.Action {

        private final FeaturePackConfig.Builder newDepBuilder;
        private final FeaturePackLocation fpl;

        AddDependencyAction(FeaturePackLocation fpl, boolean inheritConfigs, boolean inheritPackages) {
            this.fpl = fpl;
            newDepBuilder = FeaturePackConfig.builder(fpl).setInheritConfigs(inheritConfigs).setInheritPackages(inheritPackages);
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.addFeaturePackDep(newDepBuilder.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeFeaturePackDep(fpl);
        }
    }

    private class RemoveDependencyAction implements State.Action {

        private final FeaturePackLocation fpl;
        private int index;
        private FeaturePackConfig fpConfig;

        RemoveDependencyAction(FeaturePackLocation fpl) {
            this.fpl = fpl;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            index = builder.getFeaturePackDepIndex(fpl);
            fpConfig = current.getFeaturePackDep(fpl.getProducer());
            builder.removeFeaturePackDep(fpl);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.addFeaturePackDep(index, fpConfig);
        }

    }

    private abstract class AbstractAction<T> implements State.Action {

        private final Map<FeaturePackConfig, T> cf;
        private final Map<FeaturePackLocation.FPID, Integer> indexes = new HashMap<>();
        private final List<FeaturePackConfig> transitives = new ArrayList<>();
        AbstractAction(Map<FeaturePackConfig, T> cf) {
            this.cf = cf;
        }

        protected abstract boolean doAction(FeaturePackConfig.Builder fpBuilder, T id) throws ProvisioningException;

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (Entry<FeaturePackConfig, T> entry : cf.entrySet()) {
                FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.builder(entry.getKey());
                boolean doit = doAction(fpBuilder, entry.getValue());
                // this complexity is due to the fact that some fp could already have the configuration included/excluded/...
                if (doit) {
                    FeaturePackConfig cfg = fpBuilder.build();
                    if (cfg.isTransitive()) {
                        builder.removeTransitiveDep(cfg.getLocation().getFPID());
                        // Do not add back empty transitive
                        if (cfg.getLocation().getBuild() != null || !isEmptyConfig(cfg)) {
                            builder.addFeaturePackDep(cfg);
                        }
                        transitives.add(entry.getKey());
                    } else {
                        int index = builder.getFeaturePackDepIndex(entry.getKey().getLocation());
                        indexes.put(entry.getKey().getLocation().getFPID(), index);
                        builder.removeFeaturePackDep(entry.getKey().getLocation());
                        builder.addFeaturePackDep(index, fpBuilder.build());
                    }
                }
            }
        }

        private boolean isEmptyConfig(FeaturePackConfig cfg) {
            return !cfg.hasDefinedConfigs() && !cfg.hasExcludedConfigs()
                    && !cfg.hasExcludedPackages() && !cfg.hasFullModelsExcluded()
                    && !cfg.hasFullModelsIncluded() && !cfg.hasIncludedConfigs()
                    && !cfg.hasIncludedPackages() && !cfg.hasPatches() && cfg.isInheritPackages()
                    && cfg.isInheritConfigs();
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (Entry<FeaturePackConfig, T> entry : cf.entrySet()) {
                Integer index = indexes.get(entry.getKey().getLocation().getFPID());
                // index could be null if doAction failed or did not execute.
                if (index != null) {
                    builder.removeFeaturePackDep(entry.getKey().getLocation());
                    builder.addFeaturePackDep(index, entry.getKey());
                }
            }
            for (FeaturePackConfig t : transitives) {
                // Empty transitive are not added, so could no more exist
                if (builder.hasTransitiveFeaturePackDep(t.getLocation().getProducer())) {
                    builder.removeTransitiveDep(t.getLocation().getFPID());
                }
                builder.addFeaturePackDep(t);
            }
        }

    }

    private class IncludeConfigurationAction extends AbstractAction<ConfigId> {

        IncludeConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            try {
                if (fpBuilder.isDefaultConfigExcluded(id)) {
                    fpBuilder.removeExcludedDefaultConfig(id);
                }
                fpBuilder.includeDefaultConfig(id);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    private class ExcludeConfigurationAction extends AbstractAction<ConfigId> {

        ExcludeConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            if (fpBuilder.isDefaultConfigIncluded(id)) {
                fpBuilder.removeIncludedDefaultConfig(id);
            }
            fpBuilder.excludeDefaultConfig(id);
            return true;
        }
    }

    private class RemoveExcludedConfigurationAction extends AbstractAction<ConfigId> {

        RemoveExcludedConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            try {
                fpBuilder.removeExcludedDefaultConfig(id);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    private class RemoveIncludedConfigurationAction extends AbstractAction<ConfigId> {

        RemoveIncludedConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            try {
                fpBuilder.removeIncludedDefaultConfig(id);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }


    private class IncludePackageAction extends AbstractAction<String> {

        IncludePackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                if (fpBuilder.isPackageExcluded(pkg)) {
                    fpBuilder.removeExcludedPackage(pkg);
                }
                fpBuilder.includePackage(pkg);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    private class ExcludePackageAction extends AbstractAction<String> {

        ExcludePackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                if (fpBuilder.isPackageIncluded(pkg)) {
                    fpBuilder.removeIncludedPackage(pkg);
                }
                fpBuilder.excludePackage(pkg);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    private class ExcludePackageFromNewTransitiveAction implements State.Action {

        private final String pkg;
        private final FeaturePackLocation loc;
        ExcludePackageFromNewTransitiveAction(ProducerSpec producer, String pkg) {
            this.pkg = pkg;
            // New transitive are created without build ID.
            loc = FeaturePackLocation.fromString(producer.toString());
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            FeaturePackConfig config = FeaturePackConfig.transitiveBuilder(loc).excludePackage(pkg).build();
            builder.addFeaturePackDep(config);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeTransitiveDep(loc.getFPID());
        }
    }

    private class IncludePackageInNewTransitiveAction implements State.Action {

        private final String pkg;
        private final FeaturePackLocation loc;

        IncludePackageInNewTransitiveAction(ProducerSpec producer, String pkg) {
            this.pkg = pkg;
            // New transitive are created without build ID.
            loc = FeaturePackLocation.fromString(producer.toString());
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            FeaturePackConfig config = FeaturePackConfig.transitiveBuilder(loc).includePackage(pkg).build();
            builder.addFeaturePackDep(config);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeTransitiveDep(loc.getFPID());
        }
    }

    private class RemoveIncludedPackageAction extends AbstractAction<String> {

        RemoveIncludedPackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                fpBuilder.removeIncludedPackage(pkg);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    private class RemoveExcludedPackageAction extends AbstractAction<String> {

        RemoveExcludedPackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                fpBuilder.removeExcludedPackage(pkg);
            } catch (ProvisioningDescriptionException ex) {
                // already added.
                // just ignore.
                return false;
            }
            return true;
        }
    }

    State.Action removeDependency(FeaturePackLocation fpl) throws
            ProvisioningException {
        return new RemoveDependencyAction(fpl);
    }

    State.Action addDependency(PmSession pmSession, String name, FeaturePackLocation fpl,
            boolean inheritConfigs, boolean inheritPackages) throws
            ProvisioningException, IOException {
        AddDependencyAction action = new AddDependencyAction(fpl, inheritConfigs, inheritPackages);
        return action;
    }

    State.Action includeConfiguration(Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new IncludeConfigurationAction(cf);
    }

    State.Action removeIncludedConfiguration(Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveIncludedConfigurationAction(cf);
    }

    State.Action excludeConfiguration(Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new ExcludeConfigurationAction(cf);
    }

    State.Action removeExcludedConfiguration(Map<FeaturePackConfig, ConfigId> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveExcludedConfigurationAction(cf);
    }

    State.Action includePackage(String pkg, FeaturePackConfig cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        Map<FeaturePackConfig, String> map = new HashMap<>();
        map.put(cf, pkg);
        return new IncludePackageAction(map);
    }

    State.Action removeIncludedPackage(Map<FeaturePackConfig, String> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveIncludedPackageAction(cf);
    }

    State.Action excludePackage(String pkg, FeaturePackConfig cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        Map<FeaturePackConfig, String> map = new HashMap<>();
        map.put(cf, pkg);
        return new ExcludePackageAction(map);
    }

    State.Action removeExcludedPackage(Map<FeaturePackConfig, String> cf) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveExcludedPackageAction(cf);
    }

    State.Action excludePackageFromNewTransitive(ProducerSpec producer, String pkg) {
        return new ExcludePackageFromNewTransitiveAction(producer, pkg);
    }

    State.Action includePackageInNewTransitive(ProducerSpec producer, String pkg) {
        return new IncludePackageInNewTransitiveAction(producer, pkg);
    }
}
