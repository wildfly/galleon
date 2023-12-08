/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.maingrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.cmd.InstalledProducerCompleter;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.ALL_DEPENDENCIES_OPTION_NAME;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.AllDepsOptionActivator;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.FPOptionActivator;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.FP_OPTION_NAME;

/**
 *
 * @author jdenise@redhat.com
 */
public class UpdateCommand extends AbstractProvisionWithPlugins {

    public static final String YES_OPTION_NAME = "yes";

    public UpdateCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        ProcessedOption includeAll = ProcessedOptionBuilder.builder().name(ALL_DEPENDENCIES_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                optionType(OptionType.BOOLEAN).
                description(HelpDescriptions.UPDATE_DEPENDENCIES).
                completer(FileOptionCompleter.class).
                activator(AllDepsOptionActivator.class).
                required(false).
                build();
        options.add(includeAll);
        ProcessedOption yes = ProcessedOptionBuilder.builder().name(YES_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                optionType(OptionType.BOOLEAN).
                description(HelpDescriptions.UPDATE_NO_CONFIRMATION).
                completer(FileOptionCompleter.class).
                required(false).
                shortName('y').
                build();
        options.add(yes);
        ProcessedOption fp = ProcessedOptionBuilder.builder().name(FP_OPTION_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description(HelpDescriptions.UPDATE_FP).
                required(false).
                completer(InstalledProducerCompleter.class).
                activator(FPOptionActivator.class).
                build();
        options.add(fp);
        return options;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreUpdateCommand";
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // No option to validate.
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't be called");
    }

    @Override
    protected String getName() {
        return "update";
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.UPDATE;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }
}
