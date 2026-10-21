/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.util.function.Function;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateGetInfoCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateGetInfoCommand implements GalleonCoreExecution<ProvisioningSession, StateGetInfoCommand> {

    @Override
    public void execute(ProvisioningSession session, StateGetInfoCommand command) throws CommandExecutionException {
        try {
            ProvisioningConfig config = session.getContainer().getProvisioningConfig();
            Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer> supplier
                    = new Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer>() {
                public FeatureContainer apply(ProvisioningLayout<FeaturePackLayout> layout) {
                    return session.getState().getContainer();
                }
            };
            StateInfoUtil.displayInfo(session, session.getCommandInvocation(), null, config, command.getType(), supplier);
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.infoFailed(), ex);
        }
    }
}
