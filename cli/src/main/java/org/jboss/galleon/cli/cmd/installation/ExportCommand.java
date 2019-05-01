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
package org.jboss.galleon.cli.cmd.installation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlWriter;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "export", description = HelpDescriptions.EXPORT)
public class ExportCommand extends AbstractInstallationCommand {

    @Argument(required = false,
            description = HelpDescriptions.EXPORT_FILE)
    private File file;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (file != null) {
            final Path targetFile = file.toPath();
            try {
                getManager(invoc.getPmSession()).exportProvisioningConfig(targetFile);
            } catch (ProvisioningException | IOException e) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
            invoc.println("Provisioning file generated in " + targetFile);
        } else {
            ByteArrayOutputStream output = null;
            try {
                ProvisioningConfig config = getManager(invoc.getPmSession()).getProvisioningConfig();
                output = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
                ProvisioningXmlWriter.getInstance().write(config, writer);
            } catch (Exception e) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
            try {
                invoc.println(output.toString(StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new CommandExecutionException(invoc.getPmSession(), CliErrors.exportProvisionedFailed(), e);
            }
        }
    }

}
