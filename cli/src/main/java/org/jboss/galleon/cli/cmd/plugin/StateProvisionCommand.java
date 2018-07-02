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
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.AbstractFeaturePackCommand.DIR_OPTION_NAME;
import static org.jboss.galleon.cli.AbstractFeaturePackCommand.VERBOSE_OPTION_NAME;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
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
public class StateProvisionCommand extends AbstractDynamicCommand {

    public static class FileActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getState() == null;
        }
    }

    private AeshContext ctx;

    public StateProvisionCommand(PmSession pmSession) {
        super(pmSession, true, false);
    }

    public void setAeshContext(AeshContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String getId(PmSession session) throws CommandExecutionException {
        // We can't cache anything. If the state is in memory
        // then we already have a runtime, so fast.
        // If a file is provided, then we rebuild the runtime
        // to retrieve the plugins.
        return null;
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
            ProvisioningConfig config = ProvisioningXmlParser.parse(getAbsolutePath(file, ctx));
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
    protected List<ProcessedOption> getStaticOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description("File describing the desired provisioned state.").
                type(String.class).
                optionType(OptionType.ARGUMENT).
                completer(FileOptionCompleter.class).
                activator(FileActivator.class).
                build());
        options.add(ProcessedOptionBuilder.builder().name(DIR_OPTION_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description("Target installation directory.").
                completer(FileOptionCompleter.class).
                build());
        options.add(ProcessedOptionBuilder.builder().name(VERBOSE_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                description("Whether or not the output should be verbose").
                optionType(OptionType.BOOLEAN).
                build());
        return options;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new StateNoExplorationActivator();
    }

    @Override
    protected void doValidateOptions() throws CommandExecutionException {
        String filePath = getFile();
        if (filePath != null) {
            if (!Files.exists(Paths.get(filePath))) {
                throw new CommandExecutionException(filePath + " doesn't exist");
            }
        }
    }

    private boolean isVerbose() {
        return contains(VERBOSE_OPTION_NAME);
    }

    private String getFile() {
        String file = (String) getValue(ARGUMENT_NAME);
        if (file == null) {
            // Check in argument, that is the option completion case.
            file = getArgumentValue();
        }
        return file;
    }

    private String getDir() {
        return (String) getValue(DIR_OPTION_NAME);
    }

    @Override
    protected void runCommand(PmCommandInvocation invoc, Map<String, String> options) throws CommandExecutionException {
        if (isVerbose()) {
            invoc.getPmSession().enableMavenTrace(true);
        }
        try {
            if (invoc.getPmSession().getState() != null) {
                State state = invoc.getPmSession().getState();
                try {
                    getManager(invoc).provision(state.getConfig(), options);
                } catch (ProvisioningException ex) {
                    throw new CommandExecutionException(ex);
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
                    throw new CommandExecutionException("Provisioning failed", e);
                }
            }
        } finally {
            invoc.getPmSession().enableMavenTrace(false);
        }

        Path home = getInstallationHome(invoc.getAeshContext());
        if (Files.exists(home) && invoc.getPmSession().getState() != null) {
            try {
                invoc.println("Installation done in " + home.toFile().getCanonicalPath());
            } catch (IOException ex) {
                throw new CommandExecutionException(ex);
            }
        } else if (invoc.getPmSession().getState() != null) {
            invoc.println("Nothing to install");
        }
    }

    private ProvisioningManager getManager(PmCommandInvocation session) throws ProvisioningException {
        return session.getPmSession().newProvisioningManager(getInstallationHome(session.getAeshContext()), isVerbose());
    }

    private Path getInstallationHome(AeshContext context) {
        return getDir() == null ? PmSession.getWorkDir(context) : getAbsolutePath(getDir(), context);
    }

    private Path getAbsolutePath(String path, AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return path == null ? PmSession.getWorkDir(context) : workDir.resolve(path);
    }
}
