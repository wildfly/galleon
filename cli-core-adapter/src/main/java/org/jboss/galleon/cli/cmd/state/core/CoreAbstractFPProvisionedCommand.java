/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractFPProvisionedCommand<T extends AbstractFPProvisionedCommand> extends CoreAbstractStateCommand<T> {

    public abstract ProducerSpec getProducer(ProvisioningSession session, T command) throws CommandExecutionException;

    @Override
    protected void runCommand(ProvisioningSession session, State state, T command) throws IOException, ProvisioningException, CommandExecutionException {
        FeaturePackConfig cf = getProvisionedFP(session, command);
        runCommand(session, state, cf, command);
    }

    public FeaturePackConfig getProvisionedFP(ProvisioningSession session, T command) throws CommandExecutionException {
        ProducerSpec producer = getProducer(session, command);
        return getProvisionedFPConfig(producer, session, command);
    }

    public static FeaturePackConfig getProvisionedFPConfig(ProducerSpec producer,
            ProvisioningSession session,
            AbstractFPProvisionedCommand command) throws CommandExecutionException {
        if (producer == null) {
            return null;
        }
        ProvisioningConfig config = session.getState().getConfig();
        for (FeaturePackConfig dep : config.getFeaturePackDeps()) {
            if (dep.getLocation().getProducer().equals(producer)) {
                return dep;
            }
        }
        for (FeaturePackConfig dep : config.getTransitiveDeps()) {
            if (dep.getLocation().getProducer().equals(producer)) {
                return dep;
            }
        }
        return null;
    }

    protected abstract void runCommand(ProvisioningSession session, State state,
            FeaturePackConfig config, T command) throws IOException, ProvisioningException, CommandExecutionException;

}
