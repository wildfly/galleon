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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import static org.jboss.galleon.cli.AbstractStateCommand.VERBOSE_OPTION_NAME;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 * An abstract command that discover plugin options based on the fp or stream
 * argument.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractPluginsCommand extends AbstractDynamicCommand {

    static final String RESOLUTION_MESSAGE = "Resolving options";

    public AbstractPluginsCommand(PmSession pmSession) {
        super(pmSession, true, true);
    }

    protected boolean isVerbose() {
        return contains(VERBOSE_OPTION_NAME);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        if (isVerbose()) {
            session.getPmSession().enableMavenTrace(true);
        }
        try {
            FeaturePackLocation loc = pmSession.getResolvedLocation(getId(pmSession));
            runCommand(session, options, loc);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(ex.getLocalizedMessage(), ex);
        } finally {
            session.getPmSession().enableMavenTrace(false);
        }
    }

    protected abstract void runCommand(PmCommandInvocation session, Map<String, String> options,
            FeaturePackLocation loc) throws CommandExecutionException;

    protected OptionActivator getArgumentActivator() {
        return null;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // side effect is to resolve artifact.
        String fpl = getId(pmSession);
        if (fpl == null) {
            throw new CommandExecutionException("Missing feature-pack");
        }
    }

    @Override
    protected List<ProcessedOption> getStaticOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description("FP Location").
                type(String.class).
                required(true).
                optionType(OptionType.ARGUMENT).
                activator(getArgumentActivator()).
                completer(FPLocationCompleter.class).
                build());
        options.add(ProcessedOptionBuilder.builder().name(VERBOSE_OPTION_NAME).
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
        FeaturePackLocation fpl = pmSession.getResolvedLocation(id);
        Set<PluginOption> pluginOptions = getPluginOptions(fpl);
        for (PluginOption opt : pluginOptions) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired(), opt.isAcceptsValue());
            options.add(dynOption);
        }
        return options;
    }

    protected abstract Set<PluginOption> getPluginOptions(FeaturePackLocation loc) throws ProvisioningException;

    protected abstract Path getInstallationHome(AeshContext ctx);

    protected ProvisioningManager getManager(PmCommandInvocation session) throws ProvisioningException {
        return session.getPmSession().newProvisioningManager(getInstallationHome(session.getAeshContext()), isVerbose());
    }

    @Override
    protected String getId(PmSession session) throws CommandExecutionException {
        String streamName = (String) getValue(ARGUMENT_NAME);
        if (streamName == null) {
            // Check in argument or option, that is the option completion case.
            streamName = getArgumentValue();
        }
        if (streamName != null) {
            try {
                return session.getResolvedLocation(streamName).toString();
            } catch (ProvisioningException ex) {
                // Ok, no id set.
            }
        }
        return null;
    }
}
