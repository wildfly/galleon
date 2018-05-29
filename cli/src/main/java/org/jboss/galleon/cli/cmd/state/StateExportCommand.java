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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import static org.jboss.galleon.cli.AbstractFeaturePackCommand.DIR_OPTION_NAME;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

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

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false, activator = DirActivator.class,
            description = "Installation directory.")
    private String installationDir;

    @Argument(completer = FileOptionCompleter.class, required = false,
            description = "Xml to generate the provisioning config to.")
    private Resource file;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (file != null) {
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
        } else {
            ByteArrayOutputStream output = null;
            try {
                ProvisioningConfig config = invoc.getPmSession().getState() != null
                        ? invoc.getPmSession().getState().getConfig() : getManager(invoc).getProvisioningConfig();
                output = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(output);
                ProvisioningXmlWriter.getInstance().write(config, writer);
            } catch (Exception e) {
                throw new CommandExecutionException("Failed to export provisioned state", e);
            }
            invoc.println(output.toString());
        }
    }

    private ProvisioningManager getManager(PmCommandInvocation session) throws ProvisioningException {
        return ProvisioningManager.builder()
                .addArtifactResolver(session.getPmSession().getArtifactResolver())
                .setInstallationHome(getTargetDir(session.getAeshContext()))
                .setMessageWriter(new DefaultMessageWriter(session.getOut(), session.getErr(), false))
                .build();
    }

    private Path getTargetDir(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return installationDir == null ? PmSession.getWorkDir(context) : workDir.resolve(installationDir);
    }

}
