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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionCommand;

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
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreProvisionCommand";
    }

    @Override
    public String getCoreVersion(PmSession session) throws ProvisioningException {
        String file = getFile();
        try {
            return session.getGalleonBuilder().getCoreVersion(getAbsolutePath(file, session.getAeshContext()));
        } catch (IOException ex) {
            throw new ProvisioningException(ex);
        }
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
            try {
                if (!Files.exists(getAbsolutePath(filePath, invoc.getConfiguration().getAeshContext()))) {
                    throw new CommandExecutionException(filePath + " doesn't exist");
                }
            } catch (IOException ex) {
                throw new CommandExecutionException(ex.getMessage());
            }
        }
    }
    public Path getProvisioningFile() throws IOException {
        String file = getFile();
        if (file == null) {
            return null;
        }
        return getAbsolutePath(file, pmSession.getAeshContext());
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
        throw new CommandExecutionException("SHOULDN'T be called");
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
