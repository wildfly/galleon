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
package org.jboss.galleon.cli.cmd.maingrp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionCommand;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 *
 * @author jdenise@redhat.com
 */
public class ProvisionCommand extends AbstractProvisionCommand {

    public ProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.PROVISION;
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        ProvisioningRuntime rt;
        Set<ProvisioningOption> opts;
        String file = getFile();
        if (file == null) {
            return Collections.emptyList();
        }
        ProvisioningConfig config = ProvisioningXmlParser.parse(getAbsolutePath(file, pmSession.getAeshContext()));
        opts = pmSession.getResolver().get(null, PluginResolver.newResolver(pmSession, config)).getInstall();
        for (ProvisioningOption opt : opts) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired(), opt.isAcceptsValue());
            options.add(dynOption);
        }
        return options;
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description(HelpDescriptions.PROVISION_FILE).
                type(String.class).
                optionType(OptionType.ARGUMENT).
                completer(FileOptionCompleter.class).
                build());
        return options;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        String filePath = getFile();
        if (filePath != null) {
            if (!Files.exists(getAbsolutePath(filePath, invoc.getConfiguration().getAeshContext()))) {
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
        String file = getFile();
        if (file == null) {
            throw new CommandExecutionException("No provisioning file provided.");
        }
        final Path provisioningFile = getAbsolutePath(file, invoc.getConfiguration().getAeshContext());
        if (!Files.exists(provisioningFile)) {
            throw new CommandExecutionException("Failed to locate provisioning file " + provisioningFile.toAbsolutePath());
        }
        try {
            getManager(invoc).provision(provisioningFile, options);
        } catch (ProvisioningException e) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.provisioningFailed(), e);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
