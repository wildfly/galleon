/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.AddUniverseCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreAddUniverseCommand extends AbstractInstallationCommand<AddUniverseCommand> {

    @Override
    public void execute(ProvisioningSession context, AddUniverseCommand command) throws CommandExecutionException {
        try {
            context.getUniverse().
                    addUniverse(Util.lookupInstallationDir(context.getCommandInvocation().getConfiguration().getAeshContext(),
                            command.getTargetDirArg() == null ? null : command.getTargetDirArg().toPath()),
                            command.getName(), command.getFactory(), command.getLocation());
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.addUniverseFailed(), ex);
        }
    }
}
