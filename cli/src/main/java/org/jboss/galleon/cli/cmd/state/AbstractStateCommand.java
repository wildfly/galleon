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
package org.jboss.galleon.cli.cmd.state;

import java.io.IOException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractStateCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (invoc.getPmSession().getState() == null) {
            throw new CommandExecutionException("No Provisioning session");
        }
        State session = invoc.getPmSession().getState();
        try {
            runCommand(invoc, session);
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(ex);
        }
    }

    protected abstract void runCommand(PmCommandInvocation invoc, State session) throws IOException, ProvisioningException, CommandExecutionException;
}
