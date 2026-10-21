/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "exit", description = HelpDescriptions.EXIT)
public class PmExitCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        session.stop();
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.OTHERS;
    }

}
