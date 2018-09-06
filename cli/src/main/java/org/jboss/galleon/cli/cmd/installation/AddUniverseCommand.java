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
package org.jboss.galleon.cli.cmd.installation;

import java.io.File;
import java.io.IOException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import static org.jboss.galleon.cli.cmd.maingrp.AbstractProvisioningCommand.DIR_OPTION_NAME;
import org.jboss.galleon.cli.cmd.state.StateAddUniverseCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-universe", description = HelpDescriptions.ADD_UNIVERSE)
public class AddUniverseCommand extends StateAddUniverseCommand {

    @Option(name = DIR_OPTION_NAME, required = false,
            description = HelpDescriptions.INSTALLATION_DIRECTORY)
    protected File targetDirArg;

    @Override
    protected void runCommand(PmCommandInvocation commandInvocation) throws CommandExecutionException {
        try {
            commandInvocation.getPmSession().getUniverse().addUniverse(targetDirArg, name, factory, location);
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(commandInvocation.getPmSession(), CliErrors.addUniverseFailed(), ex);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return null;
    }

}
