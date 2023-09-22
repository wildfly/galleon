/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.installation.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.ExportCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreExportCommand extends AbstractInstallationCommand<ExportCommand> {

    @Override
    public void execute(ProvisioningSession context, ExportCommand command) throws CommandExecutionException {
        ProvisioningManager mgr = null;
        try {
            mgr = getManager(context, command);
        } catch (ProvisioningException e) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.exportProvisionedFailed(), e);
        }
        if (command.getFile() != null) {
            final Path targetFile = command.getFile().toPath();
            try {
                mgr.exportProvisioningConfig(targetFile);
            } catch (ProvisioningException | IOException e) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
            context.getCommandInvocation().println("Provisioning file generated in " + targetFile);
        } else {
            ByteArrayOutputStream output = null;
            try {
                ProvisioningConfig config = mgr.getProvisioningConfig();
                output = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
                ProvisioningXmlWriter.getInstance().write(config, writer);
            } catch (IOException | XMLStreamException | ProvisioningException e) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
            try {
                context.getCommandInvocation().println(output.toString(StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
        }
    }

}
