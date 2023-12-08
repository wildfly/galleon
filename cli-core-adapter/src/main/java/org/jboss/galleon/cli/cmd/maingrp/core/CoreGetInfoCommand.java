/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
