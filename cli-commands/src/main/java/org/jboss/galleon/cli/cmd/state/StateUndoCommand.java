/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;

/**
 * Dual command, applies in case of edit mode or not.
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "undo", description = HelpDescriptions.UNDO_STATE)
public class StateUndoCommand extends AbstractStateCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateUndoCommand";
    }

}
