/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CommandDomain;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name = "cd", description = HelpDescriptions.CD)
public class CdCommand extends PmSessionCommand {

    @Argument(description = HelpDescriptions.CD_PATH)
    private File path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (path == null) {
            return;
        }
        Path p = path.toPath();
        if (path.getName().equals("-")) {
            Path previous = session.getPmSession().getPreviousDirectory();
            if (previous == null) {
                return;
            }
            p = previous;
        }
        try {
            session.getPmSession().setCurrentDirectory(p);
            session.setPrompt(session.getPmSession().buildPrompt());
        } catch (IOException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.OTHERS;
    }
}
