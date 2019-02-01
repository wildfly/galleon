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
package org.jboss.galleon.cli;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jboss.galleon.cli.config.Configuration;
import org.aesh.command.AeshCommandRuntimeBuilder;
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandNotFoundHandler;
import org.aesh.command.CommandRuntime;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.converter.ConverterInvocation;
import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.invocation.CommandInvocationProvider;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.registry.MutableCommandRegistry;
import org.aesh.command.settings.Settings;
import org.aesh.command.settings.SettingsBuilder;
import org.aesh.command.shell.Shell;
import org.aesh.command.validator.ValidatorInvocation;
import org.aesh.readline.ReadlineConsole;
import org.aesh.utils.Config;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.terminal.CliShellInvocationProvider;
import org.jboss.galleon.cli.terminal.CliTerminalConnection;
import org.jboss.galleon.cli.terminal.InteractiveInvocationProvider;
import org.jboss.galleon.cli.terminal.OutputInvocationProvider;

/**
 *
 * @author Alexey Loubyansky
 */
public class CliMain {

    static {
        // Must be called prior any logging usage in order to
        // rely on JBOSS log manager if no manager already set.
        enableJBossLogManager();
    }

    public static void main(String[] args) {
        Arguments arguments = Arguments.parseArguments(args);
        boolean interactive = arguments.getCommand() == null && arguments.getScriptFile() == null;
        PmSession pmSession = null;
        try {
            pmSession = new PmSession(Configuration.parse(arguments.getOptions()), interactive);
            CliTerminalConnection connection = new CliTerminalConnection();
            pmSession.setConnection(connection.getConnection());
            if (arguments.isHelp()) {
                try {
                    CommandRuntime<? extends Command, ? extends CommandInvocation> runtime =
                            newRuntime(pmSession, connection);
                    connection.getOutput().println(HelpSupport.getToolHelp(pmSession, runtime.getCommandRegistry()));
                } finally {
                    connection.close();
                }
                return;
            }
            if (interactive) {
                // XXX jfdenise, paging could be made configurable with an option.
                startInteractive(pmSession, connection, true);
            } else {
                try {
                    runCommands(pmSession, connection, arguments);
                } finally {
                    connection.close();
                }
            }
        } catch (Throwable ex) {
            try {
                if (pmSession != null) {
                    PmSessionCommand.printException(pmSession, ex);
                } else if (ex instanceof RuntimeException) {
                    ex.printStackTrace(System.err);
                } else {
                    System.err.println(ex);
                }
            } finally {
                System.exit(1);
            }
        }
    }

    private static void enableJBossLogManager() {
        if (System.getProperty("java.util.logging.manager") == null) {
            System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        }
    }

    private static void runCommands(PmSession pmSession, CliTerminalConnection connection, Arguments arguments) throws Throwable {
        CommandRuntime runtime = newRuntime(pmSession, connection);
        pmSession.getUniverse().disableBackgroundResolution();
        pmSession.throwException();

        try {
            if (arguments.getScriptFile() != null) {
                String file = arguments.getScriptFile();
                if (file.isEmpty()) {
                    throw new Exception(CliErrors.emptyOption(Arguments.SCRIPT_FILE));
                }
                Path f = Util.resolvePath(pmSession.getAeshContext(), file);
                if (!Files.exists(f)) {
                    throw new Exception(CliErrors.unknownFile(f.toString()));
                } else if (!Files.isRegularFile(f)) {
                    throw new Exception(CliErrors.notFile(f.toString()));
                }
                List<String> commands = Files.readAllLines(f);
                for (String cmd : commands) {
                    if (cmd.startsWith("#")) {
                        continue;
                    }
                    connection.getOutput().println(Config.getLineSeparator() + cmd);
                    runtime.executeCommand(cmd);
                }
            } else if (arguments.getCommand() != null) {
                runtime.executeCommand(arguments.getCommand());
            }
        } catch (Throwable ex) {
            // Remove the wrapper used when re-throwing the exception.
            if (ex instanceof CommandException) {
                ex = ex.getCause();
            }
            throw ex;
        }
    }

    private static void startInteractive(PmSession pmSession,
            CliTerminalConnection connection, boolean paging) throws Throwable {
        pmSession.setOut(connection.getOutput());
        pmSession.setErr(connection.getOutput());
        // Side effect is to resolve plugins.
        pmSession.getUniverse().resolveBuiltinUniverse();

        Settings<? extends Command, ? extends CommandInvocation,
                ? extends ConverterInvocation,
                ? extends CompleterInvocation,
                ? extends ValidatorInvocation,
                ? extends OptionActivator,
                ? extends CommandActivator> settings = buildSettings(pmSession,
                connection, new InteractiveInvocationProvider(pmSession, paging));

        ReadlineConsole console = new ReadlineConsole(settings);

        pmSession.setAeshContext(console.context());
        console.setPrompt(pmSession.buildPrompt());

        // connection is automatically closed when exit command or Ctrl-D
        console.start();
    }

    private static Settings<? extends Command,
        ? extends CommandInvocation,
        ? extends ConverterInvocation,
        ? extends CompleterInvocation,
        ? extends ValidatorInvocation,
        ? extends OptionActivator,
        ? extends CommandActivator> buildSettings(PmSession pmSession, CliTerminalConnection connection,
            CommandInvocationProvider<PmCommandInvocation> provider) throws CommandLineParserException {
        Settings<? extends Command, ? extends CommandInvocation,
                ? extends ConverterInvocation,
                ? extends CompleterInvocation,
                ? extends ValidatorInvocation,
                ? extends OptionActivator,
                ? extends CommandActivator> settings = SettingsBuilder.builder().
                logging(false).
                commandNotFoundHandler(new CommandNotFoundHandler() {
                    @Override
                    public void handleCommandNotFound(String line, Shell shell) {
                        shell.writeln(CliErrors.commandNotFound(line));
                        CliLogging.commandNotFound(line);
                    }
                }).
                commandRegistry(buildRegistry(pmSession)).
                enableOperatorParser(true).
                persistHistory(true).
                commandActivatorProvider(pmSession).
                historyFile(pmSession.getPmConfiguration().getHistoryFile()).
                echoCtrl(false).
                enableExport(false).
                enableAlias(false).
                completerInvocationProvider(pmSession).
                optionActivatorProvider(pmSession).
                commandInvocationProvider(provider).
                connection(connection == null ? null : connection.getConnection()).
                enableSearchInPaging(true).
                build();
        return settings;
    }

    private static CommandRegistry buildRegistry(PmSession pmSession) throws CommandLineParserException {
        MutableCommandRegistry registry = (MutableCommandRegistry) new AeshCommandRegistryBuilder().create();
        ToolModes modes = ToolModes.getModes(pmSession, registry);
        pmSession.setModes(modes);
        return registry;
    }

    // A runtime attached to cli terminal connection to execute a single command.
    private static CommandRuntime<? extends Command, ? extends CommandInvocation> newRuntime(PmSession session,
            CliTerminalConnection connection) throws CommandLineParserException {
        return newRuntime(session, connection, connection.getOutput(), new CliShellInvocationProvider(session, connection));
    }

    // Used by tests. Tests don't rely on advanced output/input.
    public static CommandRuntime<? extends Command, ? extends CommandInvocation> newRuntime(PmSession session,
            PrintStream out) throws CommandLineParserException {
        return newRuntime(session, null, out, new OutputInvocationProvider(session));
    }

    private static CommandRuntime<? extends Command, ? extends CommandInvocation> newRuntime(PmSession session,
            CliTerminalConnection connection, PrintStream out,
            CommandInvocationProvider<PmCommandInvocation> provider) throws CommandLineParserException {
        AeshCommandRuntimeBuilder builder = AeshCommandRuntimeBuilder.builder();
        builder.settings(buildSettings(session, connection, provider));
        session.setOut(out);
        session.setErr(out);
        @SuppressWarnings("unchecked")
        CommandRuntime<? extends Command, ? extends CommandInvocation> runtime =
                builder.build();
        session.setAeshContext(runtime.getAeshContext());
        return runtime;
    }
}
