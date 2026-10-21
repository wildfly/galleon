/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration;

import org.aesh.command.CommandDefinition;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.FPDependentCommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "define-config", description = HelpDescriptions.DEFINE_CONFIG, activator = FPDependentCommandActivator.class)
public class StateDefineConfigCommand extends AbstractStateCommand {

    public class ModelCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

        @Override
        public void complete(PmCompleterInvocation t) {
            t.getPmSession().getState().complete(this, t);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreModelCompleter";
        }

    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Option(required = true, description = HelpDescriptions.CONFIGURATION_MODEL, completer = ModelCompleter.class)
    private String model;

    @Option(required = true, description = HelpDescriptions.CONFIGURATION_NAME)
    private String name;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreStateDefineConfigCommand";
    }
}
