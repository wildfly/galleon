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
@CommandDefinition(name = "persist-changes", description = HelpDescriptions.PERSIST_CHANGES)
public class PersistChangesCommand extends AbstractInstallationCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CorePersistChangesCommand";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
