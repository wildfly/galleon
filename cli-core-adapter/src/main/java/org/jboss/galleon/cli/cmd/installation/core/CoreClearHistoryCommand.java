/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.ClearHistoryCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreClearHistoryCommand extends AbstractInstallationCommand<ClearHistoryCommand> {

    @Override
    public void execute(ProvisioningSession context, ClearHistoryCommand command) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(context, command);
            mgr.clearStateHistory();
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.clearHistoryFailed(), ex);
        }
    }

}
