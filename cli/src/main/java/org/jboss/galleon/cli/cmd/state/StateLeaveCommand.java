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

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "leave", description = HelpDescriptions.LEAVE_STATE, activator = StateCommandActivator.class)
public class StateLeaveCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (invoc.getPmSession().getContainer() == null) {
            throw new CommandExecutionException("Nothing to leave.");
        }
        if (invoc.getPmSession().getState() != null) {
            invoc.getPmSession().getState().close();
            invoc.getPmSession().setState(null);
        } else if (invoc.getPmSession().getExploredContainer() != null) {
            invoc.getPmSession().setExploredContainer(null);
        }
        invoc.getPmSession().setCurrentPath(null);
        invoc.setPrompt(PmSession.buildPrompt(invoc.getAeshContext()));
    }

}
