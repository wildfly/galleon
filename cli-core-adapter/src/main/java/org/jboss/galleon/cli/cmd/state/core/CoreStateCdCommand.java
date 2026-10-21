/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateCdCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;

/**
 *
 * @author Alexey Loubyansky
 */
public class CoreStateCdCommand extends CoreAbstractStateCommand<StateCdCommand> {

    private void cdFp(ProvisioningSession session, String path) throws CommandExecutionException, PathParserException, PathConsumerException {
        String currentPath = session.getCurrentPath();
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(session.getContainer(), true);
        if (path.startsWith("" + PathParser.PATH_SEPARATOR)) {
            session.setCurrentPath(null);
        } else if (path.equals("..")) {
            if (currentPath == null) {
                throw new CommandExecutionException("No path entered");
            }
            if (currentPath.equals("" + PathParser.PATH_SEPARATOR)) {
                return;
            }
            currentPath = currentPath.substring(0, currentPath.length() - 1);
            int i = currentPath.lastIndexOf("" + PathParser.PATH_SEPARATOR);
            if (i < 0) {
                path = "" + PathParser.PATH_SEPARATOR;
            } else {
                path = currentPath.substring(0, i);
            }
            if (path.isEmpty()) {
                path = "" + PathParser.PATH_SEPARATOR;
            }
            session.setCurrentPath(null);
        } else {
            path = currentPath + path;
        }
        PathParser.parse(path, consumer);
        Group grp = consumer.getCurrentNode(path);
        if (grp == null) {
            return;
        } else {
            if (!path.endsWith("" + PathParser.PATH_SEPARATOR)) {
                path += PathParser.PATH_SEPARATOR;
            }
            session.setCurrentPath(path);
        }
        String prompt;
        if (FeatureContainerPathConsumer.ROOT.equals(grp.getIdentity().getName())) {
            prompt = "" + PathParser.PATH_SEPARATOR;
        } else {
            prompt = grp.getIdentity().getName() + PathParser.PATH_SEPARATOR;
        }
        session.getCommandInvocation().setPrompt(session.getPmSession().buildPrompt(prompt));
    }

    @Override
    protected void runCommand(ProvisioningSession invoc, State session, StateCdCommand command) throws IOException, ProvisioningException, CommandExecutionException {
       String path = command.getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        try {
            cdFp(invoc, path);
        } catch (PathParserException | PathConsumerException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.enterFPFailed(), ex);
        }
    }
}
