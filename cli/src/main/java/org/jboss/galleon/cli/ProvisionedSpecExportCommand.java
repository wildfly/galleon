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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.io.Resource;
import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
@CommandDefinition(name="export", description="Saves current provisioned spec into the specified file.")
public class ProvisionedSpecExportCommand extends ProvisioningCommand {

    @Argument(description = "File to save the provisioned spec too.", required = true)
    private Resource fileArg;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        if (fileArg == null) {
            throw new CommandExecutionException("Missing required file path argument.");
        }

        final Resource specResource = fileArg.resolve(session.getAeshContext().getCurrentWorkingDirectory()).get(0);
        final Path targetFile = Paths.get(specResource.getAbsolutePath());

        try {
            getManager(session).exportProvisioningConfig(targetFile);
        } catch (ProvisioningException | IOException e) {
            throw new CommandExecutionException("Failed to export provisioned state", e);
        }
    }
}
