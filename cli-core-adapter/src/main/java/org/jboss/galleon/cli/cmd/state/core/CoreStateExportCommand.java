/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.core;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateExportCommand;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateExportCommand implements GalleonCoreExecution<ProvisioningSession, StateExportCommand> {

    @Override
    public void execute(ProvisioningSession context, StateExportCommand command) throws CommandExecutionException {
        if (command.getFile() != null) {
            final Path targetFile = command.getFile().toPath();
            State session = context.getState();
            try {
                session.export(targetFile);
            } catch (Exception ex) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.exportProvisionedFailed(), ex);
            }
            context.getCommandInvocation().println("Provisioning file generated in " + targetFile);
        } else {
            ByteArrayOutputStream output = null;
            try {
                ProvisioningConfig config = context.getState().getConfig();
                output = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
                ProvisioningXmlWriter.getInstance().write(config, writer);
            } catch (Exception e) {
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
