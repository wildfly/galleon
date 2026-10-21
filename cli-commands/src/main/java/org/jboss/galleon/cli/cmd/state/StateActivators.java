/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.impl.internal.ParsedCommand;
import org.jboss.galleon.cli.AbstractCommandActivator;
import org.jboss.galleon.cli.GalleonCLICommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
public class StateActivators {

    public static class FPDependentCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$FPDependentCommandActivator";
        }
    }

    public static class ConfigDependentCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$ConfigDependentCommandActivator";
        }
    }

    public class ProvisionedPackageCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$ProvisionedPackageCommandActivator";
        }
    }

    public static class FeatureCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$FeatureCommandActivator";
        }
    }

    public static class ResetConfigCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$ResetConfigCommandActivator";
        }
    }

    public class ProvisionedDefaultConfigCommandActivator extends AbstractCommandActivator implements GalleonCLICommandActivator {

        @Override
        public boolean isActivated(ParsedCommand command) {
            return getSession().getState().isActivated(this, command);
        }

        @Override
        public String getCoreActivatorClassName() {
            return "org.jboss.galleon.cli.cmd.state.core.CoreStateActivators$ProvisionedDefaultConfigCommandActivator";
        }
    }
}
