/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.featurepack.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.featurepack.ImportCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreImportCommand implements GalleonCoreExecution<ProvisioningSession, ImportCommand> {

    @Override
    public void execute(ProvisioningSession context, ImportCommand command) throws CommandExecutionException {
        try {
            FeaturePackLocation fpl = context.getLayoutFactory().addLocal(command.getPath().toPath(), command.getInstall() == null ? true : command.getInstall());
            context.getCommandInvocation().println(fpl + " imported.");
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.importFeaturePackFailed(), ex);
        }
    }
}
