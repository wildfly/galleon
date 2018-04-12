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
package org.jboss.galleon.cli;

import java.util.logging.LogManager;
import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.command.settings.Settings;
import org.aesh.command.settings.SettingsBuilder;
import org.aesh.readline.ReadlineConsole;
import org.jboss.galleon.cli.cmd.filesystem.CdCommand;
import org.jboss.galleon.cli.cmd.filesystem.LsCommand;
import org.jboss.galleon.cli.cmd.filesystem.PmMkdir;
import org.jboss.galleon.cli.cmd.filesystem.PmRm;
import org.jboss.galleon.cli.cmd.filesystem.PwdCommand;
import org.jboss.galleon.cli.cmd.plugin.InstallCommand;
import org.jboss.galleon.cli.cmd.state.StateCommand;
import org.jboss.galleon.cli.cmd.state.feature.FeatureCommand;

/**
 *
 * @author Alexey Loubyansky
 */
public class CliMain {

    public static void main(String[] args) throws Exception {
        Configuration config = Configuration.parse();
        final PmSession pmSession = new PmSession(config);

        // Create commands that are dynamic (or contain dynamic sub commands).
        // Options are discovered at execution time
        InstallCommand install = new InstallCommand(pmSession);
        //ProvisionedSpecCommand state = new ProvisionedSpecCommand(pmSession);
        FeatureCommand feature = new FeatureCommand(pmSession);
        StateCommand state = new StateCommand(pmSession);
        AeshCommandRegistryBuilder builder = new AeshCommandRegistryBuilder()
                .command(feature)
                .command(state)
                .command(install.createCommand())
                //.command(ProvisionSpecCommand.class)
                .command(ChangesCommand.class)
                .command(UpgradeCommand.class)
                .command(UninstallCommand.class)
                .command(CdCommand.class)
                .command(PmExitCommand.class)
                .command(LsCommand.class)
                .command(PmMkdir.class)
                .command(PmRm.class)
                .command(PwdCommand.class)
                .command(UniverseCommand.class);

        StateCommand.addActionCommands(builder);

        CommandRegistry registry = builder.create();
        final Settings settings = SettingsBuilder.builder().
                logging(overrideLogging()).
                commandRegistry(registry).
                enableOperatorParser(true).
                persistHistory(true).
                commandActivatorProvider(pmSession).
                historyFile(config.getHistoryFile()).
                echoCtrl(false).
                enableExport(false).
                enableAlias(false).
                completerInvocationProvider(pmSession).
                optionActivatorProvider(pmSession).
                commandInvocationProvider(pmSession).
                build();

        // These commands require the aeshContext to properly operate
        install.setAeshContext(settings.aeshContext());
        state.setAeshContext(settings.aeshContext());

        pmSession.setOut(settings.stdOut());
        pmSession.setErr(settings.stdErr());
        ReadlineConsole console = new ReadlineConsole(settings);
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
}
