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
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name = "uninstall", description = "Uninstalls specified feature-pack", activator = NoStateCommandActivator.class)
public class UninstallCommand extends ProvisioningCommand {

    @Argument(completer = InstalledStreamCompleter.class)
    protected String streamName;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            getManager(session).uninstall(getFPID(session.getPmSession()));
        } catch (ProvisioningException e) {
            throw new CommandExecutionException("Provisioning failed", e);
        }
    }

    private FPID getFPID(PmSession session) throws CommandExecutionException {
        if (streamName == null) {
            throw new CommandExecutionException("No feature-pack provided");
        }
        try {
            return FeaturePackLocation.fromString(streamName).getFPID();
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException("Failed to parse " + streamName, e);
        }
    }
}
