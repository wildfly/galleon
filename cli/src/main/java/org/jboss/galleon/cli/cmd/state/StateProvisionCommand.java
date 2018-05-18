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
import java.nio.file.Files;
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
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "provision", description = "Install from a provided file or the current state",
        activator = StateNoExplorationActivator.class)
public class StateProvisionCommand extends PmSessionCommand {

    public static class FileActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getState() == null;
        }
    }

    @Option(completer = FileOptionCompleter.class, required = false,
            description = "Directory to install the current configuration.")
    protected String dir;

    @Option(required = false, hasValue = false)
    private Boolean verbose;

    @Argument(description = "File describing the desired provisioned state.",
            activator = FileActivator.class, required = false)
    private Resource file;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (verbose) {
            invoc.getPmSession().enableMavenTrace(true);
        }
        try {
            if (invoc.getPmSession().getState() != null) {
                State session = invoc.getPmSession().getState();
                try {
                    getManager(invoc).provision(session.getConfig());
                } catch (ProvisioningException ex) {
                    throw new CommandExecutionException(ex);
                }
            } else {
                if (file == null) {
                    throw new CommandExecutionException("Option --file is missing");
                }
                final Resource specResource = file.resolve(invoc.getAeshContext().getCurrentWorkingDirectory()).get(0);
                final Path provisioningFile = Paths.get(specResource.getAbsolutePath());
                if (!Files.exists(provisioningFile)) {
                    throw new CommandExecutionException("Failed to locate provisioning file " + provisioningFile.toAbsolutePath());
                }
                try {
                    getManager(invoc).provision(provisioningFile);
                } catch (ProvisioningException e) {
                    throw new CommandExecutionException("Provisioning failed", e);
                }
            }
        } finally {
            invoc.getPmSession().enableMavenTrace(false);
        }

        Path home = getInstallationHome(invoc.getAeshContext());
        if (Files.exists(home) && invoc.getPmSession().getState() != null) {
            try {
                invoc.println("Installation done in " + home.toFile().getCanonicalPath());
            } catch (IOException ex) {
                throw new CommandExecutionException(ex);
            }
        } else if (invoc.getPmSession().getState() != null) {
            invoc.println("Nothing to install");
        }
    }

    protected ProvisioningManager getManager(PmCommandInvocation session) {
        return ProvisioningManager.builder()
                .setArtifactResolver(session.getPmSession().getArtifactResolver())
                .setInstallationHome(getInstallationHome(session.getAeshContext()))
                .setMessageWriter(new DefaultMessageWriter(session.getOut(), session.getErr(), verbose))
                .build();
    }

    protected Path getInstallationHome(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return dir == null ? PmSession.getWorkDir(context) : workDir.resolve(dir);
    }
}
