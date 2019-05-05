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
package org.jboss.galleon.cli.cmd.maingrp;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 *
 * @author jdenise@redhat.com
 */
public class UndoCommand extends AbstractProvisionWithPlugins {

    public UndoCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected String getName() {
        return "undo";
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session);
            mgr.undo();
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.undoFailed(), ex);
        }
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.UNDO;
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
    }

}
