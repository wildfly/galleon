/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateLsCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.PathParser;

public class CoreStateLsCommand extends CoreAbstractStateCommand<StateLsCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, StateLsCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        String path = command.getPath();
        if (path == null || path.isEmpty()) {
            path = session.getCurrentPath();
        }
        FeatureContainer container = session.getContainer();
        if (!path.startsWith("" + PathParser.PATH_SEPARATOR)) {
            path = session.getCurrentPath() + path;
        }
        try {
            StateInfoUtil.printContentPath(session.getCommandInvocation(), container, path);
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.displayContentFailed(), ex);
        }
    }
}
