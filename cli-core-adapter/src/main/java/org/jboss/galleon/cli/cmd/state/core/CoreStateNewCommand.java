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

import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import static org.jboss.galleon.cli.cmd.state.StateCommand.WELCOME_STATE_MSG;
import org.jboss.galleon.cli.cmd.state.StateNewCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateNewCommand implements GalleonCoreExecution<ProvisioningSession, StateNewCommand> {

    @Override
    public void execute(ProvisioningSession session, StateNewCommand command) throws CommandExecutionException {
        State state;
        try {
            state = new State(session);
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.newStateFailed(), ex);
        }
        session.setState(state);
        if (command.getFpl() != null) {
            try {
                FeaturePackLocation fpl = session.getResolvedLocation(null, command.getFpl());
                state.addDependency(session, null, fpl, command.isInheritConfigs(), command.isInheritPackages());
            } catch (Exception ex) {
                throw new CommandExecutionException(session.getPmSession(), CliErrors.newStateFailed(), ex);
            }
        }
        session.getCommandInvocation().setPrompt(session.getCommandInvocation().getPmSession().buildPrompt(state.getPath()));
        session.getCommandInvocation().println(WELCOME_STATE_MSG);
    }

}
