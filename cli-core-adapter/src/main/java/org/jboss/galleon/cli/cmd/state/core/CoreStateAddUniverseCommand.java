/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateAddUniverseCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateAddUniverseCommand implements GalleonCoreExecution<ProvisioningSession, StateAddUniverseCommand> {

    @Override
    public void execute(ProvisioningSession context, StateAddUniverseCommand command) throws CommandExecutionException {
        try {
            context.getUniverse().addUniverse(command.getName(), command.getFactory(), command.getLocation());
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.addUniverseFailed(), ex);
        }
    }

}
