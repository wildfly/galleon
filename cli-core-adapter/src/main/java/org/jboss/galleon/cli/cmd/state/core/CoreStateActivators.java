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
package org.jboss.galleon.cli.cmd.state.core;

import java.util.Collection;
import org.aesh.command.impl.internal.ParsedCommand;
import org.jboss.galleon.cli.cmd.state.configuration.AbstractProvisionedDefaultConfigCommand;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractProvisionedPackageCommand;
import org.jboss.galleon.cli.core.GalleonCoreActivator;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateActivators {

    public static class FPDependentCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            return !context.getState().getConfig().getFeaturePackDeps().isEmpty();
        }
    }

    public static class ConfigDependentCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            return !context.getState().getContainer().getFinalConfigs().isEmpty();
        }
    }

    public static class ProvisionedPackageCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            AbstractProvisionedPackageCommand cmd = (AbstractProvisionedPackageCommand) command.command();

            for (FeaturePackConfig cf : context.getState().getConfig().getFeaturePackDeps()) {
                Collection<String> pkgs = cmd.isIncludedPackages() ? cf.getIncludedPackages() : cf.getExcludedPackages();
                if (!pkgs.isEmpty()) {
                    return true;
                }
            }
            for (FeaturePackConfig cf : context.getState().getConfig().getTransitiveDeps()) {
                Collection<String> pkgs = cmd.isIncludedPackages() ? cf.getIncludedPackages() : cf.getExcludedPackages();
                if (!pkgs.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class FeatureCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            return !context.getState().getContainer().getFinalConfigs().isEmpty();
        }
    }

    public static class ResetConfigCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            return !context.getState().getConfig().getDefinedConfigs().isEmpty();
        }
    }

    public static class ProvisionedDefaultConfigCommandActivator implements GalleonCoreActivator<ProvisioningSession> {

        @Override
        public Boolean isActivated(ParsedCommand command, ProvisioningSession context) {
            AbstractProvisionedDefaultConfigCommand cmd = (AbstractProvisionedDefaultConfigCommand) command.command();
            for (FeaturePackConfig cf : context.getState().getConfig().getFeaturePackDeps()) {
                Collection<ConfigId> configs = cmd.isIncludedConfigs() ? cf.getIncludedConfigs() : cf.getExcludedConfigs();
                if (!configs.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }
}
