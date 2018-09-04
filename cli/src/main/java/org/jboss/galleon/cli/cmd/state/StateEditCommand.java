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
package org.jboss.galleon.cli.cmd.state;

import java.io.IOException;
import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.option.Argument;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.state.StateCommand.WELCOME_STATE_MSG;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "edit", description = HelpDescriptions.EDIT_STATE)
public class StateEditCommand extends PmSessionCommand {
    @Argument(completer = FileOptionCompleter.class, required = false,
            description = HelpDescriptions.EDIT_STATE_ARG)
    protected String dir;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        State state;
        try {
            state = new State(invoc.getPmSession(), getInstallationHome(invoc.getAeshContext()));
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.readContentFailed(), ex);
        }
        invoc.getPmSession().setState(state);
        invoc.setPrompt(invoc.getPmSession().buildPrompt(state.getPath()));
        invoc.println(WELCOME_STATE_MSG);
    }

    protected Path getInstallationHome(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return dir == null ? PmSession.getWorkDir(context) : workDir.resolve(dir);
    }

}
