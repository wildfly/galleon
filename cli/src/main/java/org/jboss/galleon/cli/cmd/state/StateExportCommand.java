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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandDefinition;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.aesh.io.Resource;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.MavenArtifactRepositoryManager;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "export", description = "Generate provisioning config file from an installation or new state.",
        activator = StateNoExplorationActivator.class)
public class StateExportCommand extends PmSessionCommand {

    public static class DirActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getState() == null;
        }
    }

    @Option(name = "dir", completer = FileOptionCompleter.class, required = false, activator = DirActivator.class,
            description = "Installation directory.")
    private String installationDir;

    @Argument(completer = FileOptionCompleter.class, required = true,
            description = "Xml to generate the provisioning config to.")
    private Resource file;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        final Resource specResource = file.resolve(invoc.getAeshContext().getCurrentWorkingDirectory()).get(0);
        final Path targetFile = Paths.get(specResource.getAbsolutePath());
        if (invoc.getPmSession().getState() != null) {
            State session = invoc.getPmSession().getState();
            try {
                session.export(targetFile);
            } catch (Exception ex) {
                throw new CommandExecutionException(ex);
            }
        } else {
            try {
                getManager(invoc).exportProvisioningConfig(targetFile);
            } catch (ProvisioningException | IOException e) {
                throw new CommandExecutionException("Failed to export provisioned state", e);
            }
        }

        invoc.println("Provisioning file generated in " + targetFile);
    }

    private ProvisioningManager getManager(PmCommandInvocation session) {
        return ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(getTargetDir(session.getAeshContext()))
                .setMessageWriter(new DefaultMessageWriter(session.getOut(), session.getErr(), false))
                .build();
    }

    private Path getTargetDir(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return installationDir == null ? PmSession.getWorkDir(context) : workDir.resolve(installationDir);
    }

}
