/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aesh.command.CommandException;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.map.MapCommand;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.FeatureCommandActivator;
import org.jboss.galleon.cli.cmd.state.configuration.ProvisionedConfigurationCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
public class StateAddFeatureCommand extends AbstractDynamicCommand {

    public static class FeatureSpecIdCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

        @Override
        public void complete(PmCompleterInvocation t) {
            t.getPmSession().getState().complete(this, t);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.feature.core.CoreFeatureSpecIdCompleter";
        }

    }

    public static class AddArgumentsCompleter implements OptionCompleter<PmCompleterInvocation> {

        @Override
        public void complete(PmCompleterInvocation completerInvocation) {
            @SuppressWarnings("unchecked")
            MapCommand<PmCommandInvocation> cmd = (MapCommand<PmCommandInvocation>) completerInvocation.getCommand();
            Object value = cmd.getValue(AbstractDynamicCommand.ARGUMENT_NAME);
            if (value == null || !(value instanceof List) || ((List) value).isEmpty()) {
                new ProvisionedConfigurationCompleter().complete(completerInvocation);
            } else if ((value instanceof List) && ((List) value).size() == 1) {
                new FeatureSpecIdCompleter().complete(completerInvocation);
            }
        }

    }

    public StateAddFeatureCommand(PmSession pmSession) {
        super(pmSession, false);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.feature.core.CoreStateAddFeatureCommand";
    }

    @Override
    public GalleonCommandExecutionContext getGalleonContext(PmSession session) throws CommandException {
        return AbstractStateCommand.getGalleonExecutionContext(session);
    }

    @Override
    protected boolean canComplete(PmSession pmSession) {
        return true;
    }

    @Override
    protected String getName() {
        return "add-feature";
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.ADD_FEATURE;
    }

    public List<String> getArgument() {
        return (List<String>) getValue(ARGUMENT_NAME);
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        @SuppressWarnings("unchecked")
        List<String> args = (List<String>) getValue(ARGUMENT_NAME);
        if (args != null) {
            if (args.size() == 2) {
                return;
            }
        }
        throw new CommandExecutionException("Invalid config and feature-spec");
    }

    @Override
    protected List<ProcessedOption> getStaticOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description(HelpDescriptions.FEATURE_PATH).
                type(String.class).
                required(true).
                hasMultipleValues(true).
                optionType(OptionType.ARGUMENTS).
                completer(AddArgumentsCompleter.class).
                build());
        return options;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new FeatureCommandActivator();
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
