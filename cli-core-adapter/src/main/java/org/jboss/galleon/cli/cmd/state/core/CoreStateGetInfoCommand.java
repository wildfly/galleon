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
