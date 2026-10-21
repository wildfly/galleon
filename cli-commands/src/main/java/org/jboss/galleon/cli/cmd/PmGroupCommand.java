/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.jboss.galleon.cli.PmCommandInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
public interface PmGroupCommand extends Command<PmCommandInvocation> {

    CommandDomain getDomain();

    default CommandResult execute(PmCommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.println(CliErrors.subCommandMissing());
        return CommandResult.FAILURE;
    }
}
