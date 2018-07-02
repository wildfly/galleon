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
package org.jboss.galleon.cli.cmd.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.plugin.DiffPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class DiffCommand extends AbstractPluginsCommand {

    private static final String SRC_NAME = "src";
    private static final String TARGET_NAME = "target";
    public DiffCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options, FeaturePackLocation loc) throws CommandExecutionException {
        try {
            Path targetDirectory = toPath((String) getValue(TARGET_NAME), session.getAeshContext());
            getManager(session).exportConfigurationChanges(targetDirectory, loc.getFPID(), options);
        } catch (Exception ex) {
            throw new CommandExecutionException(ex);
        }
    }

    @Override
    protected Set<PluginOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException {
        Set<PluginOption> pluginOptions = new HashSet<>();
        FeaturePackPluginVisitor<DiffPlugin> visitor = new FeaturePackPluginVisitor<DiffPlugin>() {
            @Override
            public void visitPlugin(DiffPlugin plugin) throws ProvisioningException {
                pluginOptions.addAll(plugin.getOptions().values());
            }
        };
        runtime.visitPlugins(visitor, DiffPlugin.class);
        return pluginOptions;
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        ProcessedOption srcDir = ProcessedOptionBuilder.builder().name(SRC_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description("Customized source installation directory.").
                completer(FileOptionCompleter.class).
                build();
        options.add(srcDir);
        ProcessedOption targetDir = ProcessedOptionBuilder.builder().name(TARGET_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description("Directory to save the feature pack to.").
                completer(FileOptionCompleter.class).
                build();
        options.add(targetDir);
        return options;
    }

    @Override
    protected String getName() {
        return "diff";
    }

    @Override
    protected String getDescription() {
        return "Saves current provisioned configuration changes into a feature pack.";
    }

    @Override
    protected Path getInstallationHome(AeshContext context) {
        String srcPath = (String) getValue(SRC_NAME);
        return toPath(srcPath, context);
    }

    private Path toPath(String value, AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return value == null ? PmSession.getWorkDir(context) : workDir.resolve(value);
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new NoStateCommandActivator();
    }
}
