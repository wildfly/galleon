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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.io.Resource;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;


/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name = "provision", description = "(Re)Provisions the installation according to the specification provided in an XML file", activator = NoStateCommandActivator.class)
public class ProvisionSpecCommand extends ProvisioningCommand {

    @Argument(description = "File describing the desired provisioned state.", required = true)
    private Resource specArg;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {

        if (specArg == null) {
            throw new CommandExecutionException("Missing required file path argument.");
        }

        final Resource specResource = specArg.resolve(session.getAeshContext().getCurrentWorkingDirectory()).get(0);
        final Path provisioningFile = Paths.get(specResource.getAbsolutePath());
        if(!Files.exists(provisioningFile)) {
            throw new CommandExecutionException("Failed to locate provisioning file " + provisioningFile.toAbsolutePath());
        }
        try {
            getManager(session).provision(provisioningFile);
        } catch (ProvisioningException e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.provisioningFailed(), e);
        }
    }
}