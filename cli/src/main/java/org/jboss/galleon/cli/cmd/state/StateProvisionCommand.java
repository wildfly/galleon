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
package org.jboss.galleon.cli.cmd.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionCommand;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 * Dual command, applies in case of edit mode or not.
 *
 * @author jdenise@redhat.com
 */
public class StateProvisionCommand extends AbstractProvisionCommand {

    public StateProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.PROVISION_STATE;
    }

    @Override
    protected List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(State state) throws Exception {
        if (state == null) {
            return Collections.emptyList();
        }
        List<AbstractDynamicCommand.DynamicOption> options = new ArrayList<>();
        ProvisioningRuntime rt = state.getRuntime();
        Set<ProvisioningOption> opts = getPluginOptions(rt);
        for (ProvisioningOption opt : opts) {
            AbstractDynamicCommand.DynamicOption dynOption = new AbstractDynamicCommand.DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }

    @Override
    protected boolean canComplete(PmSession pmSession) {
        return true;
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // NO-OP
    }

    @Override
    protected void doRunCommand(PmCommandInvocation invoc, Map<String, String> options) throws CommandExecutionException {
        State state = invoc.getPmSession().getState();
        try {
            getManager(invoc).provision(state.getConfig(), options);
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.provisioningFailed(), ex);
        }

        Path home = getInstallationDirectory(invoc.getConfiguration().getAeshContext());
        if (home != null && Files.exists(home)) {
            try {
                invoc.println("Installation done in " + home.toFile().getCanonicalPath());
            } catch (IOException ex) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.retrievePath(), ex);
            }
        } else {
            invoc.println("Nothing to install");
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
