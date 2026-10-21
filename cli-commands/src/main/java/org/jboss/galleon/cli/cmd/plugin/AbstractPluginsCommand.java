/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import static org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins.VERBOSE_OPTION_NAME;
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

    public boolean isVerbose() {
        return contains(VERBOSE_OPTION_NAME);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't be called");
    }

    protected abstract void runCommand(PmCommandInvocation session, Map<String, String> options,
            FeaturePackLocation loc) throws CommandExecutionException;

    protected OptionActivator getArgumentActivator() {
        return null;
    }

    protected String getId(PmSession session) throws CommandExecutionException {
        return getFeaturePackLocation(session);
    }

    public String getFeaturePackLocation(PmSession session) throws CommandExecutionException {
        String streamName = (String) getValue(ARGUMENT_NAME);
        if (streamName == null) {
            // Check in argument or option, that is the option completion case.
            streamName = getArgumentValue();
        }
        return streamName;
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

    protected String getStreamName(PmSession session) throws CommandExecutionException {
        String streamName = (String) getValue(ARGUMENT_NAME);
        if (streamName == null) {
            // Check in argument or option, that is the option completion case.
            streamName = getArgumentValue();
        }
        return streamName;
    }
}
