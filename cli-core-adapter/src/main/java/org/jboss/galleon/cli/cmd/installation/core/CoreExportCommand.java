/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
