/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.ResetConfigCommandActivator;

/**
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "reset-config", description = HelpDescriptions.RESET_CONFIG, activator = ResetConfigCommandActivator.class)
public class StateResetConfigCommand extends AbstractStateCommand {

    @Argument(required = true, description = HelpDescriptions.CONFIGURATION_FULL_NAME,
            completer = ProvisionedConfigurationCompleter.class)
    private String configuration;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreStateResetConfigCommand";
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }
}
