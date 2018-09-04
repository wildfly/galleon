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
package org.jboss.galleon.cli.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandNotFoundException;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.parser.CommandLineParser;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.registry.CommandRegistry;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.HelpSupport;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "help", description = HelpDescriptions.HELP)
public class HelpCommand extends PmSessionCommand {

    public static class CommandCompleter implements OptionCompleter<PmCompleterInvocation> {

        @Override
        public void complete(PmCompleterInvocation completerInvocation) {
            HelpCommand cmd = (HelpCommand) completerInvocation.getCommand();
            String mainCommand = null;
            if (cmd.command != null) {
                if (cmd.command.size() > 1) {
                    // Nothing to add.
                    return;
                }
                mainCommand = cmd.command.get(0);
            }
            String buff = completerInvocation.getGivenCompleteValue();
            List<String> allAvailable = HelpSupport.getAvailableCommands(cmd.registry, false, true);
            List<String> candidates = new ArrayList<>();
            if (mainCommand == null) {
                if (buff == null || buff.isEmpty()) {
                    candidates.addAll(allAvailable);
                } else {
                    for (String c : allAvailable) {
                        if (c.startsWith(buff)) {
                            candidates.add(c);
                        }
                    }
                }
            } else {
                try {
                    CommandLineParser<? extends Command> p = cmd.registry.getCommand(mainCommand, null).getParser();
                    for (CommandLineParser child : p.getAllChildParsers()) {
                        if (child.getProcessedCommand().name().startsWith(buff)) {
                            CommandActivator childActivator = child.getProcessedCommand().getActivator();
                            if (childActivator == null
                                    || childActivator.isActivated(new ParsedCommand(child.getProcessedCommand()))) {
                                candidates.add(child.getProcessedCommand().name());
                            }
                        }
                    }
                } catch (CommandNotFoundException ex) {
                    // XXX OK, no command, no sub command.
                }
            }

            Collections.sort(candidates);
            completerInvocation.addAllCompleterValues(candidates);
        }

    }

    @Arguments(description = HelpDescriptions.HELP_COMMAND_NAME, completer = CommandCompleter.class)
    private List<String> command;

    private CommandRegistry<? extends Command, ? extends CommandInvocation> registry;

    public HelpCommand() {
    }

    public void setRegistry(CommandRegistry<? extends Command, ? extends CommandInvocation> registry) {
        this.registry = registry;
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (command == null || command.isEmpty()) {
            try {
                session.println(HelpSupport.getHelpCommandHelp(registry, session.getShell()));
            } catch (IOException ex) {
                throw new CommandExecutionException(ex.getLocalizedMessage());
            }
        } else {
            StringBuilder builder = new StringBuilder();
            for (String str : command) {
                builder.append(str).append(" ");
            }
            session.println(session.getHelpInfo(builder.toString()));
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.OTHERS;
    }
}
