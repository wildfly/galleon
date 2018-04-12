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

import static org.jboss.galleon.cli.cmd.AbstractDynamicCommand.ARGUMENT_NAME;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.aesh.command.CommandException;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GavCompleter;
import org.jboss.galleon.cli.MavenArtifactRepositoryManager;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.ProvisioningFeaturePackCommand;
import org.jboss.galleon.cli.StreamCompleter;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 * An abstract command that discover plugin options based on the fp or stream
 * argument.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractPluginsCommand extends AbstractDynamicCommand {

    private static final String VERBOSE_NAME = "verbose";
    private static final String FP_NAME = "fp";
    private AeshContext ctx;

    public AbstractPluginsCommand(PmSession pmSession) {
        super(pmSession);
    }

    public void setAeshContext(AeshContext ctx) {
        this.ctx = ctx;
    }

    protected boolean isVerbose() {
        return contains(VERBOSE_NAME);
    }

    @Override
    protected void doValidateOptions() throws CommandException {
        String id = getId(pmSession);
        if (id == null) {
            throw new CommandException("feature-pack has not been set.");
        }
    }

    @Override
    protected List<ProcessedOption> getStaticOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description("stream name").
                type(String.class).
                optionType(OptionType.ARGUMENT).
                completer(StreamCompleter.class).
                activator(ProvisioningFeaturePackCommand.StreamNameActivator.class).
                build());
        options.add(ProcessedOptionBuilder.builder().name(FP_NAME).
                hasValue(true).
                description("Feature-pack maven gav").
                type(String.class).
                optionType(OptionType.NORMAL).
                completer(GavCompleter.class).
                activator(ProvisioningFeaturePackCommand.FPActivator.class).
                build());

        options.add(ProcessedOptionBuilder.builder().name(VERBOSE_NAME).
                hasValue(false).
                type(Boolean.class).
                description("Whether or not the output should be verbose").
                optionType(OptionType.BOOLEAN).
                build());
        options.addAll(getOtherOptions());
        return options;
    }

    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state, String id) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        ProvisioningManager manager = getManager(ctx);
        FeaturePackConfig config = FeaturePackConfig.forGav(ArtifactCoords.newGav(id));
        ProvisioningConfig provisioning = ProvisioningConfig.builder().addFeaturePackDep(config).build();
        ProvisioningRuntime runtime = manager.getRuntime(provisioning, null, Collections.emptyMap());
        Set<PluginOption> pluginOptions = getPluginOptions(runtime);
        for (PluginOption opt : pluginOptions) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired(), opt.isAcceptsValue());
            options.add(dynOption);
        }
        return options;
    }

    protected abstract Set<PluginOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException;

    protected abstract Path getInstallationHome(AeshContext ctx);

    private ProvisioningManager getManager(AeshContext ctx) {
        ProvisioningManager manager = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(getInstallationHome(ctx))
                .build();
        return manager;
    }

    protected ProvisioningManager getManager(PmCommandInvocation session) {
        return ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(getInstallationHome(session.getAeshContext()))
                .setMessageWriter(new DefaultMessageWriter(session.getOut(), session.getErr(), isVerbose()))
                .build();
    }

    @Override
    protected String getId(PmSession session) {
        String streamName = (String) getValue(ARGUMENT_NAME);
        String fpCoords = (String) getValue(FP_NAME);
        if (fpCoords == null && streamName == null) {
            // Check in argument, that is the option completion case.
            String val = getArgumentValue();
            if (val == null) {
                return null;
            }
            streamName = val;
        }
        String coords = null;
        if (streamName != null) {
            try {
                coords = session.getUniverses().resolveStream(streamName).toString();
            } catch (ArtifactException ex) {
                // XXX OK, null.
            }
        } else {
            coords = fpCoords;
        }
        return coords;
    }

    protected ArtifactCoords.Gav getGav(PmSession session) throws CommandExecutionException {
        String id = getId(session);
        if (id == null) {
            throw new CommandExecutionException("Stream resolution failed");
        }
        return ArtifactCoords.newGav(id);
    }
}
