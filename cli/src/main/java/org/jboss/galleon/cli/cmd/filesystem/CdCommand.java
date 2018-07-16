/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.filesystem;

import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.io.FileResource;
import org.aesh.io.Resource;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name = "cd", description = "Changes the current work dir or FP node to the specified location")
public class CdCommand extends PmSessionCommand {

    @Argument(completer = FileAndNodeCompleter.class)
    private String path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (path == null || path.isEmpty()) {
            return;
        }
        String currentPath = session.getPmSession().getCurrentPath();
        if (currentPath == null) {
            cdDir(session);
        } else {
            try {
                cdFp(session);
            } catch (PathParserException | PathConsumerException ex) {
                throw new CommandExecutionException(session.getPmSession(), CliErrors.enterFPFailed(), ex);
            }
        }
    }

    private void cdDir(PmCommandInvocation session) {
        final AeshContext aeshCtx = session.getAeshContext();
        Resource res = new FileResource(path);
        final List<Resource> files = res.resolve(aeshCtx.getCurrentWorkingDirectory());
        if (files.get(0).isDirectory()) {
            aeshCtx.setCurrentWorkingDirectory(files.get(0));
        }
        session.setPrompt(PmSession.buildPrompt(aeshCtx));
    }

    private void cdFp(PmCommandInvocation session) throws CommandExecutionException, PathParserException, PathConsumerException {
        PmSession pm = session.getPmSession();
        String currentPath = pm.getCurrentPath();
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(pm.getContainer(), true);
        if (path.startsWith("" + PathParser.PATH_SEPARATOR)) {
            pm.setCurrentPath(null);
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
            pm.setCurrentPath(null);
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
            pm.setCurrentPath(path);
        }
        String prompt;
        if (FeatureContainerPathConsumer.ROOT.equals(grp.getIdentity().getName())) {
            prompt = (pm.getContainer().getName() == null ? ""
                    : pm.getContainer().getName()) + PathParser.PATH_SEPARATOR;
        } else {
            prompt = grp.getIdentity().getName() + PathParser.PATH_SEPARATOR;
        }
        session.setPrompt(PmSession.buildPrompt(prompt));
    }
}
