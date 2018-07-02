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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFeaturePackCommand extends PmSessionCommand {

    public static final String DIR_OPTION_NAME = "dir";
    public static final String VERBOSE_OPTION_NAME = "verbose";

    public static class DirActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            String argumentValue = parsedCommand.argument().value();
            if (argumentValue != null) {
                return false;
            }
            return true;
        }
    }

    public static class FeaturePackLocationActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(DIR_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            return true;
        }
    }

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false, activator = DirActivator.class,
            description = "Installation directory.")
    protected String targetDirArg;

    @Argument(completer = FPLocationCompleter.class, activator = FeaturePackLocationActivator.class)
    protected String fpl;

    protected FeaturePackLocation getFpl(PmSession session) throws ProvisioningException {
        if (session.getState() != null) {
            return null;
        }
        return session.getResolvedLocation(fpl);
    }

    protected String getName() {
        if (fpl != null) {
            return fpl;
        }
        if (targetDirArg != null) {
            return Paths.get(targetDirArg).getFileName().toString();
        }
        return null;
    }

    protected ProvisioningManager getManager(PmSession session, AeshContext ctx) throws ProvisioningException {
        return session.newProvisioningManager(getTargetDir(ctx), false);
    }

    protected Path getTargetDir(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return targetDirArg == null ? PmSession.getWorkDir(context) : workDir.resolve(targetDirArg);
    }

    public FeatureContainer getFeatureContainer(PmSession session, AeshContext ctx) throws ProvisioningException, Exception {
        if (session.getContainer() != null) {
            return session.getContainer();
        }
        FeatureContainer container;
        FeaturePackLocation fpl = null;
        try {
            fpl = getFpl(session);
        } catch (Exception ex) {
            // Ok no fpl, try file.
        }
        ProvisioningManager manager = getManager(session, ctx);
        if (fpl != null) {
            container = FeatureContainers.fromFeaturePackId(session, manager, fpl.getFPID(), this.fpl);
        } else {
            if (manager.getProvisionedState() == null) {
                throw new CommandExecutionException("Specified directory doesn't contain an installation");
            }
            ProvisioningConfig config = manager.getProvisioningConfig();
            try (ProvisioningRuntime runtime = manager.getRuntime(config, null, Collections.emptyMap())) {
                container = FeatureContainers.fromProvisioningRuntime(session, manager, runtime);
            }
        }
        return container;
    }
}
