/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisioningCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractFPProvisioningCommand<T extends AbstractFPProvisioningCommand> extends CoreAbstractStateCommand<T> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, T command) throws IOException, ProvisioningException, CommandExecutionException {
        FeaturePackLocation fpl = session.getResolvedLocation(null, command.getFpl());
        runCommand(session, state, command, fpl);
    }

    protected abstract void runCommand(ProvisioningSession session, State state, T command, FeaturePackLocation fpl) throws IOException, ProvisioningException, CommandExecutionException;
}
