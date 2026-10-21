/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.SetHistoryLimitCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreSetHistoryLimitCommand extends AbstractInstallationCommand<SetHistoryLimitCommand> {

    @Override
    public void execute(ProvisioningSession context, SetHistoryLimitCommand command) throws CommandExecutionException {
        try {
            int s;
            try {
                s = Integer.parseInt(command.getLimit());
            } catch (NumberFormatException ex) {
                throw new CommandExecutionException(CliErrors.invalidHistoryLimit(command.getLimit()));
            }
            ProvisioningManager mgr = getManager(context, command);
            mgr.setStateHistoryLimit(s);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.setHistoryLimitFailed(), ex);
        }
    }

}
