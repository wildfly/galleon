/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.installation.AbstractInstallationCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-changes", description = HelpDescriptions.GET_CHANGES)
public class GetChangesCommand extends AbstractInstallationCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
       throw new CommandExecutionException("Shouldn't be called");
    }
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreGetChangesCommand";
    }
    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
