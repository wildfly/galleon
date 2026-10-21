/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.fp.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.fp.StateRemoveFeaturePackCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveFeaturePackCommand extends CoreAbstractStateCommand<StateRemoveFeaturePackCommand> {
    @Override
    protected void runCommand(ProvisioningSession session, State state, StateRemoveFeaturePackCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        state.removeDependency(session, session.getResolvedLocation(null, command.getFpl()));
    }

}
