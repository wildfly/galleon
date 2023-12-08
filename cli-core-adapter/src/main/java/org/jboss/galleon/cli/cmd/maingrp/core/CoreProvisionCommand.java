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
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand.DynamicOption;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.maingrp.ProvisionCommand;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 *
 * @author jdenise@redhat.com
 */
public class CoreProvisionCommand implements GalleonCoreDynamicExecution<ProvisioningSession, ProvisionCommand> {

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, ProvisionCommand cmd) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        Set<ProvisioningOption> opts;
        Path file = cmd.getProvisioningFile();
        if (file == null) {
            return Collections.emptyList();
        }
        ProvisioningConfig config = ProvisioningXmlParser.parse(file);
        opts = session.getResolver().get(null, PluginResolver.newResolver(session, config)).getInstall();
        for (ProvisioningOption opt : opts) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }
    protected ProvisioningManager getManager(ProvisioningSession session, ProvisionCommand command) throws ProvisioningException, IOException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), command.isVerbose());
    }
    @Override
    public void execute(ProvisioningSession session, ProvisionCommand command, Map<String, String> options) throws CommandExecutionException {
        final Path provisioningFile;
        try {
            provisioningFile = command.getProvisioningFile();
        } catch (IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), ex.getLocalizedMessage(), ex);
        }
        if (provisioningFile == null) {
            throw new CommandExecutionException("No provisioning file provided.");
        }
        try {
            if (!Files.exists(provisioningFile)) {
                throw new ProvisioningException("Failed to locate provisioning file " + provisioningFile.toAbsolutePath());
            }
            getManager(session, command).provision(provisioningFile, options);
        } catch (ProvisioningException | IOException e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.provisioningFailed(), e);
        }
    }
}
