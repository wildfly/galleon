/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.option.Argument;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "edit", description = "Edit an installation or a provisioning xml file", activator = NoStateCommandActivator.class)
public class StateEditCommand extends PmSessionCommand {
    @Argument(completer = FileOptionCompleter.class, required = false,
            description = "Installation directory or provisionng file.")
    protected String dir;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (invoc.getPmSession().getContainer() != null) {
            throw new CommandExecutionException("Provisioning session already set");
        }
        State session;
        try {
            session = new State(invoc.getPmSession(), getInstallationHome(invoc.getAeshContext()));
        } catch (Exception ex) {
            throw new CommandExecutionException(ex);
        }
        invoc.getPmSession().setState(session);
        invoc.setPrompt(PmSession.buildPrompt(invoc.getPmSession().getState().getPath()));
        invoc.println("Entering provisioning composition mode. Use 'feature-pack add' command to add content. Call 'leave' to leave this mode.");
    }

    protected Path getInstallationHome(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return dir == null ? PmSession.getWorkDir(context) : workDir.resolve(dir);
    }

}
