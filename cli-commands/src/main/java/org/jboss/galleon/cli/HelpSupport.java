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
package org.jboss.galleon.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aesh.command.Command;
import org.aesh.command.CommandNotFoundException;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ProcessedCommand;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.parser.CommandLineParser;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.utils.Config;
import org.jboss.galleon.cli.cmd.CommandDomain;

/**
 *
 * @author jdenise@redhat.com
 */
public class HelpSupport {
    private static final String TAB = "    ";

    public static String getToolHelp(PmSession session,
            CommandRegistry<? extends CommandInvocation> registry) throws CommandNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("== DEFAULT MODE ==").append(Config.getLineSeparator());
        session.getToolModes().setMode(ToolModes.Mode.NOMINAL);
        sb.append(buildHelp(registry, registry.getAllCommandNames(), false));
        sb.append(Config.getLineSeparator()).append("== EDIT MODE ==").append(Config.getLineSeparator());
        session.getToolModes().setMode(ToolModes.Mode.EDIT);
        sb.append(buildHelp(registry, registry.getAllCommandNames(), true));
        return sb.toString();
    }

    public static List<String> getAvailableCommands(CommandRegistry<? extends CommandInvocation> registry,
            boolean includeChilds, boolean onlyEnabled) {
        List<String> lst = new ArrayList<>();
        // First aesh
        for (String c : registry.getAllCommandNames()) {
            CommandLineParser<? extends CommandInvocation> cmdParser;
            try {
                cmdParser = registry.getCommand(c, null).getParser();
            } catch (CommandNotFoundException ex) {
                continue;
            }
            CommandActivator activator = cmdParser.getProcessedCommand().getActivator();
            if (activator == null || activator.isActivated(new ParsedCommand(cmdParser.getProcessedCommand()))) {
                if (cmdParser.isGroupCommand() && includeChilds) {
                    for (CommandLineParser child : cmdParser.getAllChildParsers()) {
                        CommandActivator childActivator = child.getProcessedCommand().getActivator();
                        if (!onlyEnabled || (childActivator == null
                                || childActivator.isActivated(new ParsedCommand(child.getProcessedCommand())))) {
                            lst.add(c + " " + child.getProcessedCommand().name());
                        }
                    }
                } else {
                    lst.add(c);
                }
            }
        }
        return lst;
    }

    public static String buildHelp(CommandRegistry<? extends CommandInvocation> registry,                                    Set<String> commands) throws CommandNotFoundException {
        return buildHelp(registry, commands, true);
    }
    private static String buildHelp(CommandRegistry<? extends CommandInvocation> registry,
            Set<String> commands, boolean footer) throws CommandNotFoundException {
        TreeMap<CommandDomain, Set<String>> groupedCommands = new TreeMap<>();
        for (String command : commands) {
            CommandDomain group = CommandDomain.getDomain(registry.getCommand(command, null).getParser().getCommand());
            String commandTree = getCommandTree(registry, command);

            if (group == null) {
                group = CommandDomain.OTHERS;
            }

            Set<String> currentDescriptions = groupedCommands.get(group);

            if (currentDescriptions == null) {
                currentDescriptions = new TreeSet<>();
                groupedCommands.put(group, currentDescriptions);
            }

            currentDescriptions.add(commandTree);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<CommandDomain, Set<String>> groupedCommand : groupedCommands.entrySet()) {
            sb.append(Config.getLineSeparator());
            sb.append("== ").append(groupedCommand.getKey().getDescription()).append(" ==");
            sb.append(Config.getLineSeparator());
            for (String description : groupedCommand.getValue()) {
                sb.append(description);
                sb.append(Config.getLineSeparator());
            }
        }

        if (footer) {
            sb.append(getHelpFooter());
        }

        return sb.toString();
    }

    private static String getCommandTree(CommandRegistry<? extends CommandInvocation> registry,
            String command) throws CommandNotFoundException {
        CommandLineParser<? extends CommandInvocation> cmdParser = registry.getCommand(command, null).getParser();

        StringBuilder sb = new StringBuilder();
        ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> processedCommand
                = cmdParser.getProcessedCommand();

        sb.append(processedCommand.name());

        if (processedCommand.hasArguments() || processedCommand.hasArgument()) {
            sb.append(" <arg>");
        }

        sb.append(" ").append(trimDescription(processedCommand.description()));
        sb.append(getCommandOptions(processedCommand, 0));

        if (!cmdParser.getAllChildParsers().isEmpty()) {
            List<? extends CommandLineParser<? extends CommandInvocation>> allChildParsers = cmdParser.getAllChildParsers();

            allChildParsers.sort((Comparator<CommandLineParser<? extends CommandInvocation>>) (o1, o2) -> {
                ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> pc1
                        = o1.getProcessedCommand();
                ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> pc2
                        = o2.getProcessedCommand();
                return pc1.name().compareTo(pc2.name());
            });

            for (CommandLineParser<? extends CommandInvocation> childParser : allChildParsers) {
                ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> childProcessedCommand
                        = childParser.getProcessedCommand();
                sb.append(Config.getLineSeparator()).append("    ");
                sb.append(childProcessedCommand.name());
                if (childProcessedCommand.hasArguments() || childProcessedCommand.hasArgument()) {
                    sb.append(" <arg>");
                }
                sb.append(" ").append(trimDescription(childProcessedCommand.description()));
                sb.append(getCommandOptions(childProcessedCommand, 4));
            }
        }

        return sb.toString();
    }

    private static String getHelpFooter() {
        try {
            return getHelp(getHelpPath("help-command.txt"));
        } catch (IOException e) {
            return "Failed to read help-command.txt. " + e.getLocalizedMessage();
        }
    }

    private static String getCommandOptions(ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> command,
            int offset) {
        StringBuilder sb = new StringBuilder();
        if (command.hasOptions()) {
            TreeMap<String, String> orderedOptions = new TreeMap<>();
            sb.append(Config.getLineSeparator());

            if (command.hasArgument() || command.hasArguments()) {
                processArguments(command, offset, sb);
                // if the command has an empty or null argument we don't add a new line
                if (sb.lastIndexOf(Config.getLineSeparator()) != sb.length() - 1) {
                    sb.append(Config.getLineSeparator());
                }
            }

            List<ProcessedOption> options = command.getOptions();
            for (ProcessedOption option : options) {
                orderedOptions.put(option.name(), trimDescription(
                        option.getFormattedOption(offset + TAB.length(), 0, 80)));
            }

            for (Map.Entry<String, String> option : orderedOptions.entrySet()) {
                sb.append(option.getValue());
                if (!option.equals(orderedOptions.lastEntry())) {
                    sb.append(Config.getLineSeparator());
                }
            }
        } else if (command.hasArguments() || command.hasArgument()) {
            sb.append(Config.getLineSeparator());
            processArguments(command, offset, sb);
        }

        return sb.toString();
    }

    private static void processArguments(ProcessedCommand<? extends Command<? extends CommandInvocation>, ? extends CommandInvocation> command,
            int offset, StringBuilder sb) {
        if (command.hasArgument()) {
            if (command.getArgument().description() != null && !("".equals(command.getArgument().description()))) {
                handleOffset(offset, sb);
                sb.append(TAB).append("<arg> ").append(trimDescription(command.getArgument().description()));
            }
        } else if (command.hasArguments()) {
            if (command.getArguments().description() != null && !("".equals(command.getArguments().description()))) {
                handleOffset(offset, sb);
                sb.append(TAB).append("<arg> ").append(trimDescription(command.getArguments().description()));
            }
        }
    }

    private static String trimDescription(String description) {
        if (description.contains(".")) {
            description = description.substring(0, description.indexOf("."));
        }
        return description;
    }

    private static void handleOffset(int offset, StringBuilder sb) {
        if (offset > 0) {
            if ((offset % TAB.length()) == 0) {
                sb.append(TAB);
            } else {
                for (int i = 0; i < offset; i++) {
                    sb.append(" ");
                }
            }
        }
    }

    private static String getHelp(String path) throws IOException {
        InputStream helpInput = CliMain.class.getResourceAsStream(path);
        if (helpInput != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(helpInput, StandardCharsets.UTF_8))) {
                try {
                    StringBuilder builder = new StringBuilder();
                    String helpLine = reader.readLine();
                    while (helpLine != null) {
                        builder.append(helpLine).append(Config.getLineSeparator());
                        helpLine = reader.readLine();
                    }
                    return builder.toString();
                } catch (java.io.IOException e) {
                    return "Failed to read " + path + ". " + e.getLocalizedMessage();
                }
            }
        } else {
            return "Failed to locate help description " + path;
        }
    }

    private static String getHelpPath(String file) {
        return "/help/" + file;
    }
}
