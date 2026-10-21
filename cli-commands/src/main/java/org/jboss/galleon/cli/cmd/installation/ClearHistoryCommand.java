/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "clear-history", description = HelpDescriptions.CLEAR_HISTORY)
public class ClearHistoryCommand extends AbstractInstallationCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.installation.core.CoreClearHistoryCommand";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
