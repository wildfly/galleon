/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;

@CommandDefinition(name = "pwd", description = HelpDescriptions.PWD)
public class StatePwdCommand extends AbstractStateCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStatePwdCommand";
    }
}
