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

import java.util.HashMap;
import java.util.Map;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.cmd.CommandDomain;
/**
 * @deprecated
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
@CommandDefinition(name = "upgrade", description = "Saves current provisioned configuration into the specified file.")
public class UpgradeCommand extends FromInstallationCommand {

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
    @Option(name = "gav", required=true, completer=GavCompleter.class,
            description = "Feature pack GAV coordinates.")
    protected String coord;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        Map<String, String> parameters = new HashMap<>(5);
        if (host != null) {
            parameters.put("host", host);
        }
        if (port != null) {
            parameters.put("port", port);
        }
        if (protocol != null) {
            parameters.put("protocol", protocol);
        }
        if (username != null) {
            parameters.put("username", username);
        }
        if (password != null) {
            parameters.put("password", password);
        }
        if (serverConfig != null) {
            parameters.put("server-config", serverConfig);
        }
        /*
        try {
            getManager(session).upgrade(ArtifactCoords.newGav(coord), parameters);
        } catch (ProvisioningException | IOException e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.exportProvisionedFailed(), e);
        }
        */
    }

    @Override
    public CommandDomain getDomain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
