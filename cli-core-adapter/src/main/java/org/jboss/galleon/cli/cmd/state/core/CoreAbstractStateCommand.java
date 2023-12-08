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
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractStateCommand<T extends AbstractStateCommand> implements GalleonCoreExecution<ProvisioningSession, T> {

    protected abstract void runCommand(ProvisioningSession invoc, State session, T command) throws IOException, ProvisioningException, CommandExecutionException;

    @Override
    public void execute(ProvisioningSession session, T command) throws CommandExecutionException {
        if (session.getState() == null) {
            throw new CommandExecutionException("No Provisioning session");
        }
        State state = session.getState();
        try {
            runCommand(session, state, command);
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.editCommandFailed(), ex);
        }
    }
}
