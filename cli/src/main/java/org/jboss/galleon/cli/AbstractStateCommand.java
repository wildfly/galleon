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
import java.util.Collections;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractStateCommand extends PmSessionCommand implements CommandWithInstallationDirectory {

    public static class DirActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            return getPmSession().getContainer() == null;
        }
    }
    public static final String DIR_OPTION_NAME = "dir";
    public static final String VERBOSE_OPTION_NAME = "verbose";

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false,
            activator = DirActivator.class, description = "Installation directory.")
    protected String targetDirArg;

    protected String getName() {
        return Paths.get(targetDirArg).getFileName().toString();
    }

    protected ProvisioningManager getManager(PmSession session) throws ProvisioningException {
        return session.newProvisioningManager(getInstallationDirectory(session.getAeshContext()), false);
    }

    @Override
    public Path getInstallationDirectory(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return targetDirArg == null ? PmSession.getWorkDir(context) : workDir.resolve(targetDirArg);
    }

    public FeatureContainer getFeatureContainer(PmSession session) throws ProvisioningException,
            CommandExecutionException, IOException {
        if (session.getContainer() != null) {
            return session.getContainer();
        }
        FeatureContainer container;
        ProvisioningManager manager = getManager(session);

        if (manager.getProvisionedState() == null) {
            throw new CommandExecutionException("Specified directory doesn't contain an installation");
        }
        ProvisioningConfig config = manager.getProvisioningConfig();
        try (ProvisioningRuntime runtime = manager.getRuntime(config, Collections.emptyMap())) {
            container = FeatureContainers.fromProvisioningRuntime(session, runtime);
        }
        return container;
    }

    protected ProvisioningConfig getProvisioningConfig(PmSession session) throws ProvisioningException, CommandExecutionException {
        if (session.getContainer() != null) {
            return session.getContainer().getProvisioningConfig();
        }
        ProvisioningManager manager = getManager(session);

        if (manager.getProvisionedState() == null) {
            throw new CommandExecutionException("Specified directory doesn't contain an installation");
        }
        return manager.getProvisioningConfig();
    }
}
