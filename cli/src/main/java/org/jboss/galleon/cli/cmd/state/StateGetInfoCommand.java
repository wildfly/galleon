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
package org.jboss.galleon.cli.cmd.state;

import java.util.function.Function;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-info", description = HelpDescriptions.GET_INFO_STATE)
public class StateGetInfoCommand extends PmSessionCommand {

    @Option(completer = InfoTypeCompleter.class, description = HelpDescriptions.INFO_TYPE)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            ProvisioningConfig config = invoc.getPmSession().getContainer().getProvisioningConfig();
            Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer> supplier
                    = new Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer>() {
                public FeatureContainer apply(ProvisioningLayout<FeaturePackLayout> layout) {
                    return invoc.getPmSession().getState().getContainer();
                }
            };
            StateInfoUtil.displayInfo(invoc, config, type, supplier);
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.infoFailed(), ex);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
