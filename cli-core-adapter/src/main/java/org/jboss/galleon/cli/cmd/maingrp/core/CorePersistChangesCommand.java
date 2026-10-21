/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.maingrp.PersistChangesCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CorePersistChangesCommand extends org.jboss.galleon.cli.cmd.installation.core.AbstractInstallationCommand<PersistChangesCommand> {

    @Override
    public void execute(ProvisioningSession context, PersistChangesCommand command) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(context, command);
            if (!mgr.persistChanges()) {
                context.getCommandInvocation().println("No changes to persist");
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
    }
}
