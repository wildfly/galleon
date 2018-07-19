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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand.RESOLUTION_MESSAGE;
import org.jboss.galleon.cli.cmd.state.StateNoExplorationActivator;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 * @author jdenise@redhat.com
 */
public class StateProvisionCommand extends AbstractProvisionWithPlugins {

    public static class FileActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getState() == null;
        }
    }

    public StateProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getName() {
        return "provision";
    }

    @Override
    protected String getDescription() {
        return "Install from a provisioning file or the current state";
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state, String id) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        ProvisioningRuntime rt;
        Set<PluginOption> opts;
        if (state != null) {
            rt = state.getRuntime();
            opts = getPluginOptions(rt);
        } else {
            String file = getFile();
            if (file == null) {
                return Collections.emptyList();
            }
            ProvisioningConfig config = ProvisioningXmlParser.parse(getAbsolutePath(file, pmSession.getAeshContext()));
            opts = pmSession.getResolver().get(id, PluginResolver.newResolver(pmSession, config),
                    RESOLUTION_MESSAGE).getInstall();
        }
        for (PluginOption opt : opts) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired(), opt.isAcceptsValue());
            options.add(dynOption);
        }
        return options;
    }

    private Set<PluginOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException {
        Set<PluginOption> pluginOptions = new HashSet<>();
        FeaturePackPluginVisitor<InstallPlugin> visitor = new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                pluginOptions.addAll(plugin.getOptions().values());
            }
        };
        runtime.visitPlugins(visitor, InstallPlugin.class);
        return pluginOptions;
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description("File describing the desired provisioned state.").
                type(String.class).
                optionType(OptionType.ARGUMENT).
                completer(FileOptionCompleter.class).
                activator(FileActivator.class).
                build());
        return options;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new StateNoExplorationActivator();
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        String filePath = getFile();
        if (filePath != null) {
            if (!Files.exists(Paths.get(filePath))) {
                throw new CommandExecutionException(filePath + " doesn't exist");
            }
        }
    }

    private String getFile() {
        String file = (String) getValue(ARGUMENT_NAME);
        if (file == null) {
            // Check in argument, that is the option completion case.
            file = getArgumentValue();
        }
        return file;
    }

    @Override
    protected void doRunCommand(PmCommandInvocation invoc, Map<String, String> options) throws CommandExecutionException {
        if (invoc.getPmSession().getState() != null) {
            State state = invoc.getPmSession().getState();
            try {
                getManager(invoc).provision(state.getConfig(), options);
            } catch (ProvisioningException ex) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.provisioningFailed(), ex);
            }
        } else {
            String file = getFile();
            if (file == null) {
                throw new CommandExecutionException("No provisioning file provided.");
            }
            final Path provisioningFile = getAbsolutePath(file, invoc.getAeshContext());
            if (!Files.exists(provisioningFile)) {
                throw new CommandExecutionException("Failed to locate provisioning file " + provisioningFile.toAbsolutePath());
            }
            try {
                getManager(invoc).provision(provisioningFile, options);
            } catch (ProvisioningException e) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.provisioningFailed(), e);
            }
        }

        Path home = getInstallationDirectory(invoc.getAeshContext());
        if (Files.exists(home) && invoc.getPmSession().getState() != null) {
            try {
                invoc.println("Installation done in " + home.toFile().getCanonicalPath());
            } catch (IOException ex) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.retrievePath(), ex);
            }
        } else if (invoc.getPmSession().getState() != null) {
            invoc.println("Nothing to install");
        }
    }
}
