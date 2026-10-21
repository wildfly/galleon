/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.StateActivators.ProvisionedDefaultConfigCommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-excluded-config", description = HelpDescriptions.REMOVE_EXCLUDED_CONFIG, activator = ProvisionedDefaultConfigCommandActivator.class)
public class StateRemoveExcludedConfigCommand extends AbstractProvisionedDefaultConfigCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreStateRemoveExcludedConfigCommand";
    }

    @Override
    public boolean isIncludedConfigs() {
        return false;
    }
}
