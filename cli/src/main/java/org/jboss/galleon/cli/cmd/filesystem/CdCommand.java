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

import java.io.File;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.io.FileResource;
import org.aesh.io.Resource;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name = "cd", description = HelpDescriptions.CD)
public class CdCommand extends PmSessionCommand {

    @Argument()
    private File path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (path == null) {
            return;
        }
        cdDir(session);
    }

    private void cdDir(PmCommandInvocation session) {
        final AeshContext aeshCtx = session.getConfiguration().getAeshContext();
        Resource res = new FileResource(path);
        final List<Resource> files = res.resolve(aeshCtx.getCurrentWorkingDirectory());
        if (files.get(0).isDirectory()) {
            aeshCtx.setCurrentWorkingDirectory(files.get(0));
        }
        session.setPrompt(session.getPmSession().buildPrompt());
    }
}
