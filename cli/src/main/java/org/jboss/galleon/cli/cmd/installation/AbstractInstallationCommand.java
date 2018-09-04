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
package org.jboss.galleon.cli.cmd.installation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import static org.jboss.galleon.cli.cmd.maingrp.AbstractProvisioningCommand.DIR_OPTION_NAME;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractInstallationCommand extends PmSessionCommand implements CommandWithInstallationDirectory {

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false,
            description = HelpDescriptions.INSTALLATION_DIRECTORY)
    protected String targetDirArg;

    protected ProvisioningManager getManager(PmSession session) throws ProvisioningException {
        return session.newProvisioningManager(getInstallationDirectory(session.getAeshContext()), false);
    }

    @Override
    public Path getInstallationDirectory(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return targetDirArg == null ? PmSession.getWorkDir(context) : workDir.resolve(targetDirArg);
    }

    public FeatureContainer getFeatureContainer(PmSession session, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException,
            CommandExecutionException, IOException {
        FeatureContainer container;
        ProvisioningManager manager = getManager(session);

        if (manager.getProvisionedState() == null) {
            throw new CommandExecutionException("Specified directory doesn't contain an installation");
        }
        if (layout == null) {
            ProvisioningConfig config = manager.getProvisioningConfig();
            try (ProvisioningRuntime runtime = manager.getRuntime(config, Collections.emptyMap())) {
                container = FeatureContainers.fromProvisioningRuntime(session, runtime);
            }
        } else {
            try (ProvisioningRuntime runtime = manager.getRuntime(layout, Collections.emptyMap())) {
                container = FeatureContainers.fromProvisioningRuntime(session, runtime);
            }
        }
        return container;
    }

    protected ProvisioningConfig getProvisioningConfig(PmSession session) throws ProvisioningException, CommandExecutionException {
        ProvisioningManager manager = getManager(session);

        if (manager.getProvisionedState() == null) {
            throw new CommandExecutionException("Specified directory doesn't contain an installation");
        }
        return manager.getProvisioningConfig();
    }
}
