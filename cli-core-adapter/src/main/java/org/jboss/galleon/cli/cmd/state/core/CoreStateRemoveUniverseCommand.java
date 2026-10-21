/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateRemoveUniverseCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveUniverseCommand implements GalleonCoreExecution<ProvisioningSession, StateRemoveUniverseCommand> {

    @Override
    public void execute(ProvisioningSession context, StateRemoveUniverseCommand command) throws CommandExecutionException {
        try {
            context.getUniverse().removeUniverse(command.getName());
        } catch (ProvisioningException | IOException ex) {
           throw new CommandExecutionException(context.getPmSession(), CliErrors.removeUniverseFailed(), ex);
        }
    }

}
