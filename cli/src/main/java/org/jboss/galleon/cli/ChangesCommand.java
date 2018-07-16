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
import java.util.HashMap;
import java.util.Map;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.aesh.io.Resource;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;

/**
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
@CommandDefinition(name = "changes", description = "Saves current provisioned configuration changes into the specified directory.", activator = NoStateCommandActivator.class)
public class ChangesCommand extends FromInstallationCommand {

    @Option(name = "username", required = true,
            description = "User to connect to provisionned server.")
    protected String username;
    @Option(name = "password", required = true,
            description = "Password to connect to provisionned server.")
    protected String password;
    @Option(name = "port", required = false, defaultValue="9990",
            description = "Protocol to connect to provisionned server.")
    protected String port;
    @Option(name = "host", required = false, defaultValue="127.0.0.1",
            description = "Protocol to connect to provisionned server.")
    protected String host;
    @Option(name = "protocol", required = false, defaultValue = "remote+http",
            description = "Protocol to connect to provisionned server.")
    protected String protocol;
    @Option(name = "server-config", required = false, defaultValue = "standalone.xml",
            description = "Server configuration file to use for the provisionned server.")
    protected String serverConfig;
    @Option(name = "target", required = true,
            description="Directory to export the changes to.")
    protected Resource exportDirArg;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {

        Map<String, String> options = new HashMap<>(5);
        if (host != null) {
            options.put("host", host);
        }
        if (port != null) {
            options.put("port", port);
        }
        if (protocol != null) {
            options.put("protocol", protocol);
        }
        if (username != null) {
            options.put("username", username);
        }
        if (password != null) {
            options.put("password", password);
        }
        if (serverConfig != null) {
            options.put("server-config", serverConfig);
        }
        final Resource specTargetResource = exportDirArg.resolve(session.getAeshContext().getCurrentWorkingDirectory()).get(0);
        final Path targetFile = Paths.get(specTargetResource.getAbsolutePath());
        try {
            getManager(session).exportConfigurationChanges(targetFile, null, options);
        } catch (ProvisioningException | IOException e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.exportProvisionedFailed(), e);
        }
    }
}
