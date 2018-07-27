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

import java.io.PrintStream;
import java.util.logging.LogManager;

import org.aesh.command.AeshCommandRuntimeBuilder;
import org.aesh.command.CommandRuntime;
import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.settings.Settings;
import org.aesh.command.settings.SettingsBuilder;
import org.aesh.extensions.clear.Clear;
import org.aesh.readline.ReadlineConsole;
import org.jboss.galleon.cli.cmd.featurepack.FeaturePackCommand;
import org.jboss.galleon.cli.cmd.filesystem.CdCommand;
import org.jboss.galleon.cli.cmd.filesystem.LsCommand;
import org.jboss.galleon.cli.cmd.filesystem.PmMkdir;
import org.jboss.galleon.cli.cmd.filesystem.PmRm;
import org.jboss.galleon.cli.cmd.filesystem.PwdCommand;
import org.jboss.galleon.cli.cmd.mvn.MavenCommand;
import org.jboss.galleon.cli.cmd.plugin.InstallCommand;
import org.jboss.galleon.cli.cmd.state.SearchCommand;
import org.jboss.galleon.cli.cmd.state.StateCommand;
import org.jboss.galleon.cli.cmd.state.feature.FeatureCommand;
import org.jboss.galleon.cli.cmd.universe.UniverseCommand;
import org.jboss.galleon.cli.config.Configuration;

/**
 *
 * @author Alexey Loubyansky
 */
public class CliMain {

    private static final String ENABLE_EXPERIMENTAL_FEATURES = "org.jboss.galleon.cli.enable.experimental.features";

    public static boolean experimentalFeaturesEnabled() {
        return Boolean.getBoolean(CliMain.ENABLE_EXPERIMENTAL_FEATURES);
    }

    public static void main(String[] args) throws Exception {
        Configuration config = Configuration.parse();
        final PmSession pmSession = new PmSession(config);
        // Side effect is to resolve plugins.
        pmSession.getUniverse().resolveBuiltinUniverse();

        Settings settings = buildSettings(pmSession, null);

        ReadlineConsole console = new ReadlineConsole(settings);

        pmSession.setAeshContext(console.context());
        console.setPrompt(PmSession.buildPrompt(settings.aeshContext()));
        console.start();
    }

    private static boolean overrideLogging() {
        // If the current log manager is not java.util.logging.LogManager the user has specifically overridden this
        // and we should not override logging
        return LogManager.getLogManager().getClass() == LogManager.class &&
                // The user has specified a class to configure logging, we shouldn't override it
                System.getProperty("java.util.logging.config.class") == null &&
                // The user has specified a specific logging configuration and again we shouldn't override it
                System.getProperty("java.util.logging.config.file") == null;
    }

    private static Settings buildSettings(PmSession pmSession, PrintStream out) throws CommandLineParserException {
        Settings settings = SettingsBuilder.builder().
                logging(overrideLogging()).
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
                commandInvocationProvider(pmSession).
                outputStream(out).
                build();
        pmSession.setOut(settings.stdOut());
        pmSession.setErr(settings.stdErr());
        return settings;
    }

    private static CommandRegistry buildRegistry(PmSession pmSession) throws CommandLineParserException {
        // Create commands that are dynamic (or contain dynamic sub commands).
        // Options are discovered at execution time
        InstallCommand install = new InstallCommand(pmSession);
        //ProvisionedSpecCommand state = new ProvisionedSpecCommand(pmSession);
        FeatureCommand feature = new FeatureCommand(pmSession);
        StateCommand state = new StateCommand(pmSession);
        AeshCommandRegistryBuilder builder = new AeshCommandRegistryBuilder()
                .command(Clear.class)
                .command(FeaturePackCommand.class)
                .command(MavenCommand.class)
                .command(feature)
                .command(state)
                .command(install.createCommand())
                //.command(ProvisionSpecCommand.class)
                .command(ChangesCommand.class)
                //.command(UpgradeCommand.class)
                .command(UninstallCommand.class)
                .command(CdCommand.class)
                .command(PmExitCommand.class)
                .command(LsCommand.class)
                .command(PmMkdir.class)
                .command(PmRm.class)
                .command(PwdCommand.class)
                .command(SearchCommand.class)
                .command(UniverseCommand.class);

        StateCommand.addActionCommands(builder);

        return builder.create();
    }

    public static CommandRuntime newRuntime(PmSession session, PrintStream out) throws CommandLineParserException {
        AeshCommandRuntimeBuilder builder = AeshCommandRuntimeBuilder.builder();
        builder.settings(buildSettings(session, out));
        CommandRuntime runtime = builder.build();
        session.setAeshContext(runtime.getAeshContext());
        return runtime;
    }
}
