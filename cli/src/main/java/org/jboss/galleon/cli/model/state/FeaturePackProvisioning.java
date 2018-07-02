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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;

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
            fpConfig = current.getFeaturePackDep(fpl.getChannel());
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

        AbstractAction(Map<FeaturePackConfig, T> cf) {
            this.cf = cf;
        }

        protected abstract boolean doAction(FeaturePackConfig.Builder fpBuilder, T id) throws ProvisioningException;

        protected abstract void undoAction(FeaturePackConfig.Builder fpBuilder, T id) throws ProvisioningException;

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (Entry<FeaturePackConfig, T> entry : cf.entrySet()) {
                FeaturePackConfig.Builder fpBuilder = entry.getKey().getBuilder();
                boolean doit = doAction(fpBuilder, entry.getValue());
                // this complexity is due to the fact that some fp could already have the configuration included/excluded/...
                if (doit) {
                    int index = builder.getFeaturePackDepIndex(entry.getKey().getLocation());
                    indexes.put(entry.getKey().getLocation().getFPID(), index);
                    builder.removeFeaturePackDep(entry.getKey().getLocation());
                    builder.addFeaturePackDep(index, fpBuilder.build());
                }
            }
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (Entry<FeaturePackConfig, T> entry : cf.entrySet()) {
                Integer index = indexes.get(entry.getKey().getLocation().getFPID());
                // index could be null if doAction failed or did not execute.
                if (index != null) {
                    FeaturePackConfig.Builder fpBuilder = entry.getKey().getBuilder();
                    undoAction(fpBuilder, entry.getValue());
                    builder.removeFeaturePackDep(entry.getKey().getLocation());
                    builder.addFeaturePackDep(index, fpBuilder.build());
                }
            }
        }

    }

    private class IncludeConfigurationAction extends AbstractAction<ConfigId> {

        private boolean wasExcluded = false;

        IncludeConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            try {
                if (fpBuilder.isDefaultConfigExcluded(id)) {
                    wasExcluded = true;
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            fpBuilder.removeIncludedDefaultConfig(id);
            if (wasExcluded) {
                fpBuilder.excludeDefaultConfig(id);
            }
        }

    }

    private class ExcludeConfigurationAction extends AbstractAction<ConfigId> {

        private boolean wasIncluded = false;

        ExcludeConfigurationAction(Map<FeaturePackConfig, ConfigId> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            if (fpBuilder.isDefaultConfigIncluded(id)) {
                wasIncluded = true;
                fpBuilder.removeIncludedDefaultConfig(id);
            }
            fpBuilder.excludeDefaultConfig(id);
            return true;
        }

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            fpBuilder.removeExcludedDefaultConfig(id);
            if (wasIncluded) {
                fpBuilder.includeDefaultConfig(id);
            }
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            fpBuilder.excludeDefaultConfig(id);
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, ConfigId id) throws ProvisioningException {
            fpBuilder.includeDefaultConfig(id);
        }

    }


    private class IncludePackageAction extends AbstractAction<String> {

        private boolean wasExcluded = false;

        IncludePackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                if (fpBuilder.isPackageExcluded(pkg)) {
                    wasExcluded = true;
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            fpBuilder.removeIncludedPackage(pkg);
            if (wasExcluded) {
                fpBuilder.excludePackage(pkg);
            }
        }

    }

    private class ExcludePackageAction extends AbstractAction<String> {

        private boolean wasIncluded = false;

        ExcludePackageAction(Map<FeaturePackConfig, String> cf) {
            super(cf);
        }

        @Override
        protected boolean doAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            try {
                if (fpBuilder.isPackageIncluded(pkg)) {
                    wasIncluded = true;
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            fpBuilder.removeExcludedPackage(pkg);
            if (wasIncluded) {
                fpBuilder.includePackage(pkg);
            }
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            fpBuilder.includePackage(pkg);
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

        @Override
        protected void undoAction(FeaturePackConfig.Builder fpBuilder, String pkg) throws ProvisioningException {
            fpBuilder.excludePackage(pkg);
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
}
