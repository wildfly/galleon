/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.featurepack;

import java.io.IOException;
import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "clear-cache", description = HelpDescriptions.CLEAR_CACHE)
public class ClearCacheCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().clearLayoutCache();
        } catch (IOException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
    }

}
