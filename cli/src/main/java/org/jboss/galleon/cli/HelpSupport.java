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
package org.jboss.galleon.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aesh.command.Command;
import org.aesh.command.CommandNotFoundException;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.parser.CommandLineParser;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.shell.Shell;
import org.aesh.utils.Config;

/**
 *
 * @author jdenise@redhat.com
 */
public class HelpSupport {

    private static final String AVAILABLE_COMMANDS = "$AVAILABLE_COMMANDS";
    private static final String AVAILABLE_OPTIONS = "$AVAILABLE_OPTIONS";
    private static final String TAB = "    ";

    public static String getToolHelp(CommandRegistry<? extends Command, ? extends CommandInvocation> registry, Shell shell) throws IOException {
        String staticContent = getHelp(getHelpPath("help.txt"));
        String availableOptions = format(Arguments.getToolOptions(), shell.size().getWidth());
        String availableCommands = format(getAvailableCommandAndDescriptions(registry), shell.size().getWidth());
        staticContent = staticContent.replace(AVAILABLE_OPTIONS, availableOptions);
        staticContent = staticContent.replace(AVAILABLE_COMMANDS, availableCommands);
        return staticContent;
    }

    public static String getHelpCommandHelp(CommandRegistry<? extends Command, ? extends CommandInvocation> registry, Shell shell) throws IOException {
        String staticContent = getHelp(getHelpPath("help-command.txt"));
        String availableCommands = Util.formatColumns(getAvailableCommands(registry, true),
                shell.size().getWidth(), shell.size().getWidth());
        staticContent = staticContent.replace(AVAILABLE_COMMANDS, availableCommands);
        return staticContent;
    }

    private static String format(Map<String, String> options, int width) {
        int max = 0;
        List<String> keys = new ArrayList<>();
        for (String k : options.keySet()) {
            if (k.length() > max) {
                max = k.length();
            }
            keys.add(k);
        }
        Collections.sort(keys);
        StringBuilder builder = new StringBuilder();
        int remaining = width - max - TAB.length();
        String maxPad = pad(max + TAB.length(), "");
        for (String k : keys) {
            String val = options.get(k);
            builder.append(pad(max, k)).append(TAB);
            if (val.length() <= remaining) {
                builder.append(val).append(Config.getLineSeparator());
            } else {
                int offset = remaining;
                String pad = "";
                do {
                    int limit = Math.min(offset, val.length());
                    String content = val.substring(0, limit);
                    val = val.substring(limit);
                    String toNextLine = null;
                    if (!val.isEmpty()) {
                        // cut at the last whitespace
                        int i = content.lastIndexOf(" ");
                        if (i >= 0) {
                            toNextLine = content.substring(i += 1);
                            content = content.substring(0, i);
                            val = toNextLine + val;
                        }
                    }
                    builder.append(pad).append(content).append(Config.getLineSeparator());
                    pad = maxPad;
                    offset += remaining - (toNextLine == null ? 0 : toNextLine.length());
                } while (val.length() > 0);
            }
        }
        return builder.toString();
    }

    private static String pad(int max, String k) {
        StringBuilder builder = new StringBuilder();
        builder.append(k);
        for (int i = k.length(); i < max; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public static Map<String, String> getAvailableCommandAndDescriptions(CommandRegistry<? extends Command, ? extends CommandInvocation> registry) {
        Map<String, String> commands = new HashMap<>();
        // First aesh
        for (String c : registry.getAllCommandNames()) {
            CommandLineParser<? extends Command> cmdParser;
            try {
                cmdParser = registry.getCommand(c, null).getParser();
            } catch (CommandNotFoundException ex) {
                continue;
            }
            CommandActivator activator = cmdParser.getProcessedCommand().getActivator();
            if (activator == null || activator.isActivated(new ParsedCommand(cmdParser.getProcessedCommand()))) {
                if (cmdParser.isGroupCommand()) {
                    for (CommandLineParser child : cmdParser.getAllChildParsers()) {
                        CommandActivator childActivator = child.getProcessedCommand().getActivator();
                        if (childActivator == null
                                || childActivator.isActivated(new ParsedCommand(child.getProcessedCommand()))) {
                            commands.put(c + " " + child.getProcessedCommand().name(), child.getProcessedCommand().description());
                        }
                    }
                } else {
                    commands.put(c, cmdParser.getProcessedCommand().description());
                }
            }
        }
        return commands;
    }

    public static List<String> getAvailableCommands(CommandRegistry<? extends Command, ? extends CommandInvocation> registry, boolean includeChilds) {
        List<String> lst = new ArrayList<>();
        // First aesh
        for (String c : registry.getAllCommandNames()) {
            CommandLineParser<? extends Command> cmdParser;
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
                        if (childActivator == null
                                || childActivator.isActivated(new ParsedCommand(child.getProcessedCommand()))) {
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

    private static String getHelp(String path) throws IOException {
        InputStream helpInput = CliMain.class.getResourceAsStream(path);
        if (helpInput != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(helpInput))) {
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
