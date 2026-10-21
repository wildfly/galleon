/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.state.StateLeaveCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateLeaveCommand implements GalleonCoreExecution<ProvisioningSession, StateLeaveCommand> {

    @Override
    public void execute(ProvisioningSession session, StateLeaveCommand command) throws CommandExecutionException {
        session.setState(null);
        session.getCommandInvocation().setPrompt(session.getPmSession().buildPrompt());
    }

}
