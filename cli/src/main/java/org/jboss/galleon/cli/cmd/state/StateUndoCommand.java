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
import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.option.Option;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "undo", description = "Undo the last provisioning command")
public class StateUndoCommand extends org.jboss.galleon.cli.AbstractStateCommand {

    public static class VerboseActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getContainer() == null;
        }
    }

    @Option(name = VERBOSE_OPTION_NAME, required = false, hasValue = false, activator = VerboseActivator.class)
    private boolean verbose;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            if (invoc.getPmSession().getState() == null) {
                Path p = getInstallationDirectory(invoc.getAeshContext());
                ProvisioningManager mgr = invoc.getPmSession().newProvisioningManager(p, verbose);
                mgr.undo();
            } else {
                State state = invoc.getPmSession().getState();
                state.pop(invoc.getPmSession());
                if (!state.hasActions()) {
                    throw new ProvisioningException(Errors.historyIsEmpty());
                }
            }
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.undoFailed(), ex);
        }
    }

}
