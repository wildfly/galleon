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
package org.jboss.galleon.cli.cmd.state.fp;

import java.io.IOException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisioningCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-dependency", description = HelpDescriptions.ADD_DEPENDENCY)
public class StateAddFeaturePackCommand extends AbstractFPProvisioningCommand {

    @Option(name = "default-configs-inherit", required = false, hasValue = false, description = HelpDescriptions.INCLUDE_DEFAULT_CONFIGS)
    Boolean inheritConfigs;

    @Option(name = "packages-inherit", required = false, hasValue = false, description = HelpDescriptions.INCLUDE_DEFAULT_PACKAGES)
    Boolean inheritPackages;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State session,
            FeaturePackLocation fpl) throws IOException, ProvisioningException, CommandExecutionException {
        session.addDependency(invoc.getPmSession(), this.fpl, fpl, inheritConfigs, inheritPackages);
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
