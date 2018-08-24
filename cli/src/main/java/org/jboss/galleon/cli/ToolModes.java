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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.aesh.command.container.CommandContainer;
import org.aesh.command.impl.container.AeshCommandContainerBuilder;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.MutableCommandRegistry;
import org.aesh.extensions.clear.Clear;
import org.jboss.galleon.cli.cmd.HelpCommand;
import org.jboss.galleon.cli.cmd.PmExitCommand;
import org.jboss.galleon.cli.cmd.featurepack.FeaturePackCommand;
import org.jboss.galleon.cli.cmd.filesystem.FileSystemCommand;
import org.jboss.galleon.cli.cmd.installation.InstallationCommand;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand;
import org.jboss.galleon.cli.cmd.maingrp.FindCommand;
import org.jboss.galleon.cli.cmd.maingrp.GetInfoCommand;
import org.jboss.galleon.cli.cmd.maingrp.InstallCommand;
import org.jboss.galleon.cli.cmd.maingrp.ListFeaturePacksCommand;
import org.jboss.galleon.cli.cmd.maingrp.ProvisionCommand;
import org.jboss.galleon.cli.cmd.maingrp.UndoCommand;
import org.jboss.galleon.cli.cmd.maingrp.UninstallCommand;
import org.jboss.galleon.cli.cmd.maingrp.UpdateCommand;
import org.jboss.galleon.cli.cmd.mvn.MavenCommand;
import org.jboss.galleon.cli.cmd.state.StateSearchCommand;
import org.jboss.galleon.cli.cmd.state.StateAddUniverseCommand;
import org.jboss.galleon.cli.cmd.state.StateCdCommand;
import org.jboss.galleon.cli.cmd.state.StateCommand;
import org.jboss.galleon.cli.cmd.state.StateExportCommand;
import org.jboss.galleon.cli.cmd.state.StateGetInfoCommand;
import org.jboss.galleon.cli.cmd.state.StateLeaveCommand;
import org.jboss.galleon.cli.cmd.state.StateLsCommand;
import org.jboss.galleon.cli.cmd.state.StateProvisionCommand;
import org.jboss.galleon.cli.cmd.state.StatePwdCommand;
import org.jboss.galleon.cli.cmd.state.StateRemoveUniverseCommand;
import org.jboss.galleon.cli.cmd.state.StateUndoCommand;
import org.jboss.galleon.cli.cmd.state.configuration.StateExcludeConfigCommand;
import org.jboss.galleon.cli.cmd.state.configuration.StateIncludeConfigCommand;
import org.jboss.galleon.cli.cmd.state.configuration.StateRemoveExcludedConfigCommand;
import org.jboss.galleon.cli.cmd.state.configuration.StateRemoveIncludedConfigCommand;
import org.jboss.galleon.cli.cmd.state.configuration.StateResetConfigCommand;
import org.jboss.galleon.cli.cmd.state.feature.StateAddFeatureCommand;
import org.jboss.galleon.cli.cmd.state.feature.StateRemoveFeatureCommand;
import org.jboss.galleon.cli.cmd.state.fp.StateAddFeaturePackCommand;
import org.jboss.galleon.cli.cmd.state.fp.StateRemoveFeaturePackCommand;
import org.jboss.galleon.cli.cmd.state.pkg.StateExcludePackageCommand;
import org.jboss.galleon.cli.cmd.state.pkg.StateIncludePackageCommand;
import org.jboss.galleon.cli.cmd.state.pkg.StateRemoveExcludedPackageCommand;
import org.jboss.galleon.cli.cmd.state.pkg.StateRemoveIncludedPackageCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@SuppressWarnings("unchecked")
public class ToolModes {

    public enum Mode {
        NOMINAL,
        EDIT
    };

    private final MutableCommandRegistry registry;
    private final List<CommandContainer> nominalCommands = new ArrayList<>();
    private final List<CommandContainer> commonCommands = new ArrayList<>();
    private final List<CommandContainer> editCommands = new ArrayList<>();
    private final AeshCommandContainerBuilder containerBuilder = new AeshCommandContainerBuilder<>();
    private Mode activeMode;

    private ToolModes(PmSession pmSession, MutableCommandRegistry registry) throws CommandLineParserException {
        this.registry = registry;
        HelpCommand help = new HelpCommand();
        help.setRegistry(registry);

        // Common commands
        commonCommands.add(containerBuilder.create(new Clear()));
        commonCommands.add(containerBuilder.create(new FindCommand()));
        commonCommands.add(containerBuilder.create(help));
        commonCommands.add(containerBuilder.create(new FeaturePackCommand()));
        commonCommands.add(containerBuilder.create(new ListFeaturePacksCommand()));
        commonCommands.add(containerBuilder.create(new MavenCommand()));
        commonCommands.add(containerBuilder.create(new PmExitCommand()));

        // NOMINAL MODE
        nominalCommands.add(containerBuilder.create(new CheckUpdatesCommand()));
        nominalCommands.add(containerBuilder.create(new FileSystemCommand()));
        nominalCommands.add(containerBuilder.create(new GetInfoCommand()));
        nominalCommands.add(containerBuilder.create(new InstallationCommand()));
        nominalCommands.add(new InstallCommand(pmSession).createCommand());
        nominalCommands.add(new ProvisionCommand(pmSession).createCommand());
        nominalCommands.add(containerBuilder.create(new StateCommand()));
        nominalCommands.add(containerBuilder.create(new UndoCommand()));
        nominalCommands.add(new UninstallCommand(pmSession).createCommand());
        nominalCommands.add(new UpdateCommand(pmSession).createCommand());

        // EDIT MODE
        editCommands.add(containerBuilder.create(new StateExportCommand()));
        editCommands.add(containerBuilder.create(new StateGetInfoCommand()));
        editCommands.add(containerBuilder.create(new StateLeaveCommand()));
        editCommands.add(new StateProvisionCommand(pmSession).createCommand());
        editCommands.add(containerBuilder.create(new StateSearchCommand()));
        editCommands.add(containerBuilder.create(new StateUndoCommand()));
        // Feature-pack
        editCommands.add(containerBuilder.create(new StateAddFeaturePackCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveFeaturePackCommand()));
        // Packages
        editCommands.add(containerBuilder.create(new StateExcludePackageCommand()));
        editCommands.add(containerBuilder.create(new StateIncludePackageCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveExcludedPackageCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveIncludedPackageCommand()));
        //Feature
        editCommands.add(containerBuilder.create(new StateRemoveFeatureCommand()));
        editCommands.add(new StateAddFeatureCommand(pmSession).createCommand());
        //Configs
        editCommands.add(containerBuilder.create(new StateExcludeConfigCommand()));
        editCommands.add(containerBuilder.create(new StateIncludeConfigCommand()));
        editCommands.add(containerBuilder.create(new StateResetConfigCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveExcludedConfigCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveIncludedConfigCommand()));
        // navigation
        editCommands.add(containerBuilder.create(new StateCdCommand()));
        editCommands.add(containerBuilder.create(new StateLsCommand()));
        editCommands.add(containerBuilder.create(new StatePwdCommand()));
        // universe
        editCommands.add(containerBuilder.create(new StateAddUniverseCommand()));
        editCommands.add(containerBuilder.create(new StateRemoveUniverseCommand()));

        setMode(Mode.NOMINAL);
    }

    public static ToolModes getModes(PmSession session, MutableCommandRegistry registry) throws CommandLineParserException {
        return new ToolModes(session, registry);
    }

    final void setMode(Mode mode) {
        Objects.requireNonNull(mode);
        activeMode = mode;
        clearCommands();
        registry.addAllCommandContainers(commonCommands);
        switch (mode) {
            case NOMINAL: {
                registry.addAllCommandContainers(nominalCommands);
                break;
            }
            case EDIT: {
                registry.addAllCommandContainers(editCommands);
                break;
            }
        }
    }

    public Mode getActiveMode() {
        return activeMode;
    }

    private void clearCommands() {
        Set<String> names = new HashSet<>(registry.getAllCommandNames());
        for (String name : names) {
            registry.removeCommand(name);
        }
    }
}
