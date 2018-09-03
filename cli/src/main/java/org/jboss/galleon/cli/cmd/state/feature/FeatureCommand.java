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
package org.jboss.galleon.cli.cmd.state.feature;

import java.util.ArrayList;
import java.util.List;
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.GroupCommand;
import org.aesh.command.GroupCommandDefinition;
import org.aesh.command.container.CommandContainer;
import org.aesh.command.parser.CommandLineParserException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(description = "", name = "feature", activator = FeatureCommandActivator.class)
public class FeatureCommand implements GroupCommand<PmCommandInvocation, Command> {
    private final StateAddFeatureCommand featureAddCommand;

    public FeatureCommand(PmSession pmSession) {
        this.featureAddCommand = new StateAddFeatureCommand(pmSession);
    }
    @Override
    public CommandResult execute(PmCommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.println(CliErrors.subCommandMissing());
        return CommandResult.FAILURE;
    }

    @Override
    public List<CommandContainer<Command<PmCommandInvocation>, PmCommandInvocation>> getParsedCommands() throws CommandLineParserException {
        List<CommandContainer<Command<PmCommandInvocation>, PmCommandInvocation>> commands = new ArrayList<>();
        commands.add(featureAddCommand.createCommand());
        return commands;
    }

    @Override
    public List<Command> getCommands() {
        List<Command> lst = new ArrayList<>();
        lst.add(new StateRemoveFeatureCommand());
        return lst;
    }
}
