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
import org.jboss.galleon.cli.cmd.InstalledFPLCompleter;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins;
import static org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins.DIR_OPTION_NAME;

/**
 *
 * @author jdenise@redhat.com
 */
public class UninstallCommand extends AbstractProvisionWithPlugins {

    public UninstallCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description(HelpDescriptions.FP_TO_REMOVE).
                type(String.class).
                optionType(OptionType.ARGUMENT).
                completer(InstalledFPLCompleter.class).
                build());
        return options;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreUninstallCommand";
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't be called");
    }

    @Override
    protected String getName() {
        return "uninstall";
    }

    @Override
    protected boolean canComplete(PmSession pmSession) {
        if (getFPID() == null) {
            return false;
        }
        return super.canComplete(pmSession);
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.UNINSTALL;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
    }

    public String getFPID() {
        String fpid = (String) getValue(ARGUMENT_NAME);
        if (fpid == null) {
            // Check in argument, that is the option completion case.
            fpid = getArgumentValue();
        }
        return fpid;
    }

    public String getUninstallDir() {
        String targetDirArg = (String) getValue(DIR_OPTION_NAME);
        if (targetDirArg == null) {
            // Check in argument or option, that is the option completion case.
            targetDirArg = getOptionValue(DIR_OPTION_NAME);
        }
        return targetDirArg;
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
