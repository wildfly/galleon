/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name="display", description="Prints provisioned spec for the specified installation.")
public class ProvisionedSpecDisplayCommand extends ProvisioningCommand {

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if(verbose) {
            final ProvisionedState provisionedState;
            try {
                provisionedState = getManager(session).getProvisionedState();
            } catch (ProvisioningException e) {
                throw new CommandExecutionException(session.getPmSession(), CliErrors.readProvisionedStateFailed(), e);
            }
            if (provisionedState == null || !provisionedState.hasFeaturePacks()) {
                return;
            }
            for (ProvisionedFeaturePack fp : provisionedState.getFeaturePacks()) {
                session.println(fp.getFPID().toString());
            }
        } else {
            final ProvisioningConfig provisionedState;
            try {
                provisionedState = getManager(session).getProvisioningConfig();
            } catch (ProvisioningException e) {
                throw new CommandExecutionException(session.getPmSession(), CliErrors.readProvisionedStateFailed(), e);
            }
            if (provisionedState == null || !provisionedState.hasFeaturePackDeps()) {
                return;
            }
            for (FeaturePackConfig fp : provisionedState.getFeaturePackDeps()) {
                session.println(fp.getLocation().toString());
            }
        }
    }
}
