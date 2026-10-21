/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.installation.AbstractInstallationCommand;
import org.jboss.galleon.cli.cmd.state.InfoTypeCompleter;

/**
 *
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-info", description = HelpDescriptions.GET_INFO)
public class GetInfoCommand extends AbstractInstallationCommand {

    @Option(completer = InfoTypeCompleter.class, description = HelpDescriptions.INFO_TYPE)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("SHOULDN'T be called");
    }

    public String getType() {
        return type;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreGetInfoCommand";
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
