/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.util.function.Function;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.maingrp.GetInfoCommand;
import org.jboss.galleon.cli.cmd.state.core.StateInfoUtil;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.cmd.installation.core.AbstractInstallationCommand;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;

/**
 *
 * @author jdenise
 */
public class CoreGetInfoCommand extends AbstractInstallationCommand<GetInfoCommand> {

    @Override
    public void execute(ProvisioningSession session, GetInfoCommand cmd) throws CommandExecutionException {
        try {
            Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer> supplier
                    = new Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer>() {
                public FeatureContainer apply(ProvisioningLayout<FeaturePackLayout> layout) {
                    try {
                        return getFeatureContainer(session, layout, cmd);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            ProvisioningManager mgr = getManager(session, cmd);
            StateInfoUtil.displayInfo(session, session.getCommandInvocation(), mgr.getInstallationHome(), mgr.getProvisioningConfig(), cmd.getType(), supplier);
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), ex.getLocalizedMessage(), ex);
        }
    }
}
