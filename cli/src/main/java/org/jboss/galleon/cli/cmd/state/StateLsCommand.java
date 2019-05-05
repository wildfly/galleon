/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.StateFullPathCompleter;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.path.PathParser;

@CommandDefinition(name = "ls", description = HelpDescriptions.LS)
public class StateLsCommand extends PmSessionCommand {

    @Argument(completer = StateFullPathCompleter.class, description = HelpDescriptions.FP_PATH)
    private String path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (path == null || path.isEmpty()) {
            path = session.getPmSession().getCurrentPath();
        }
        FeatureContainer container = session.getPmSession().getContainer();
        if (!path.startsWith("" + PathParser.PATH_SEPARATOR)) {
            path = session.getPmSession().getCurrentPath() + path;
        }
        try {
            StateInfoUtil.printContentPath(session, container, path);
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.displayContentFailed(), ex);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
