/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CommandDomain;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractStateCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't have been called");
    }

    @Override
    public GalleonCommandExecutionContext getGalleonContext(PmSession session) throws CommandException {
        return getGalleonExecutionContext(session);
    }

    public static GalleonCommandExecutionContext getGalleonExecutionContext(PmSession session) throws CommandException {
        GalleonCommandExecutionContext ctx = session.getState();
        if (ctx == null) {
            throw new CommandException("No state exist, command not available");
        }
        return ctx;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
