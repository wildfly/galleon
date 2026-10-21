/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "set-history-limit", description = HelpDescriptions.SET_HISTORY_LIMIT)
public class SetHistoryLimitCommand extends AbstractInstallationCommand {

    // We can't rely on Integer injection due to AESH-479.
    @Argument(required = true, description = HelpDescriptions.HISTORY_LIMIT)
    private String limit;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.installation.core.CoreSetHistoryLimitCommand";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the limit
     */
    public String getLimit() {
        return limit;
    }

}
