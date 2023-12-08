/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
