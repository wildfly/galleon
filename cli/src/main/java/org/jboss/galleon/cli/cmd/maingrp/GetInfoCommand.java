/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.maingrp;

import java.io.IOException;
import java.util.function.Function;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.installation.AbstractInstallationCommand;
import org.jboss.galleon.cli.cmd.state.InfoTypeCompleter;
import org.jboss.galleon.cli.cmd.state.StateInfoUtil;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;

/**
 *
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-info", description = HelpDescriptions.GET_INFO)
public class GetInfoCommand extends AbstractInstallationCommand {

    @Option(completer = InfoTypeCompleter.class, description = HelpDescriptions.INFO_TYPE)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer> supplier
                    = new Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer>() {
                public FeatureContainer apply(ProvisioningLayout<FeaturePackLayout> layout) {
                    try {
                        return getFeatureContainer(invoc.getPmSession(), layout);
                    } catch (CommandExecutionException | ProvisioningException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            ProvisioningManager mgr = getManager(invoc.getPmSession());
            StateInfoUtil.displayInfo(invoc, mgr.getInstallationHome(), mgr.getProvisioningConfig(), type, supplier);
        } catch (ProvisioningException | CommandExecutionException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.infoFailed(), ex);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
