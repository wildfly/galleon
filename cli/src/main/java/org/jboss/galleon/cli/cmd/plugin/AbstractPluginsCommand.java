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
package org.jboss.galleon.cli.cmd.plugin;

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
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import static org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins.VERBOSE_OPTION_NAME;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 * An abstract command that discover plugin options based on the fp or stream
 * argument.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractPluginsCommand extends AbstractDynamicCommand implements CommandWithInstallationDirectory {

    public AbstractPluginsCommand(PmSession pmSession) {
        super(pmSession, true);
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
            final String id = getId(pmSession);
            final FeaturePackLocation loc = id == null ? null : pmSession.
                    getResolvedLocation(getInstallationDirectory(session.
                            getConfiguration().getAeshContext()), id);
            runCommand(session, options, loc);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.resolveLocationFailed(), ex);
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
                description(HelpDescriptions.FP_LOCATION).
                type(String.class).
                required(true).
                optionType(OptionType.ARGUMENT).
                activator(getArgumentActivator()).
                completer(FPLocationCompleter.class).
                build());
        options.add(ProcessedOptionBuilder.builder().name(VERBOSE_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                description(HelpDescriptions.VERBOSE).
                optionType(OptionType.BOOLEAN).
                build());
        options.addAll(getOtherOptions());
        return options;
    }

    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        FeaturePackLocation fpl = pmSession.getResolvedLocation(getInstallationDirectory(pmSession.getAeshContext()),
                getId(pmSession));
        Set<ProvisioningOption> pluginOptions = getPluginOptions(fpl);
        for (ProvisioningOption opt : pluginOptions) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }

    protected abstract Set<ProvisioningOption> getPluginOptions(FeaturePackLocation loc) throws ProvisioningException;

    protected ProvisioningManager getManager(PmCommandInvocation session) throws ProvisioningException {
        return session.getPmSession().newProvisioningManager(getInstallationDirectory(session.
                getConfiguration().getAeshContext()), isVerbose());
    }

    protected String getId(PmSession session) throws CommandExecutionException {
        String streamName = (String) getValue(ARGUMENT_NAME);
        if (streamName == null) {
            // Check in argument or option, that is the option completion case.
            streamName = getArgumentValue();
        }
        if (streamName != null) {
            try {
                return session.getResolvedLocation(getInstallationDirectory(session.getAeshContext()),
                        streamName).toString();
            } catch (ProvisioningException ex) {
                // Ok, no id set.
            }
        }
        return null;
    }
}
