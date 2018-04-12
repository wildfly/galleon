/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import static org.jboss.galleon.cli.ProvisioningFeaturePackCommand.FP_OPTION_NAME;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFeaturePackCommand extends PmSessionCommand {

    private static final String DIR_OPTION_NAME = "dir";

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
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(FP_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            return true;
        }
    }

    public static class StreamNameActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(DIR_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            opt = parsedCommand.findLongOptionNoActivatorCheck(FP_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            return true;
        }
    }

    public static class FPGavActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(DIR_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            String argumentValue = parsedCommand.argument().value();
            return argumentValue == null;
        }
    }

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false, activator = DirActivator.class,
            description = "Installation directory.")
    protected String targetDirArg;

    @Argument(completer = StreamCompleter.class, activator = StreamNameActivator.class)
    protected String streamName;

    @Option(name = FP_OPTION_NAME, completer = GavCompleter.class, activator = FPGavActivator.class)
    protected String fpCoords;

    protected ArtifactCoords.Gav getGav(PmSession session) throws CommandExecutionException {
        if (session.getState() != null) {
            return null;
        }
        if (fpCoords == null && streamName == null) {
            throw new CommandExecutionException("Stream name or feature-pack coordinates must be set");
        }
        if (fpCoords != null && streamName != null) {
            throw new CommandExecutionException("Only one of stream name or feature-pack coordinates must be set");
        }

        String coords;
        if (streamName != null) {
            try {
                coords = session.getUniverses().resolveStream(streamName).toString();
            } catch (ArtifactException ex) {
                throw new CommandExecutionException("Stream resolution failed", ex);
            }
        } else {
            coords = fpCoords;
        }
        return ArtifactCoords.newGav(coords);
    }

    protected String getName() {
        if (streamName != null) {
            return streamName;
        }
        if (targetDirArg != null) {
            return Paths.get(targetDirArg).getFileName().toString();
        }
        if (fpCoords != null) {
            return fpCoords;
        }
        return null;
    }

    protected ProvisioningManager getManager(AeshContext ctx) {
        ProvisioningManager.Builder builder = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance());
        builder.setInstallationHome(getTargetDir(ctx));
        return builder.build();
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
        ArtifactCoords.Gav gav = null;
        try {
            gav = getGav(session);
        } catch (Exception ex) {
            // Ok no gav, try file.
        }
        ProvisioningManager manager = getManager(ctx);
        if (gav != null) {
            container = FeatureContainers.fromFeaturePackGav(session, manager, gav, streamName);
        } else {
            if (manager.getProvisionedState() == null) {
                throw new CommandExecutionException("Specified directory doesn't contain an installation");
            }
            ProvisioningConfig config = manager.getProvisioningConfig();
            ProvisioningRuntime runtime = manager.getRuntime(config, null, Collections.emptyMap());
            container = FeatureContainers.fromProvisioningRuntime(session, manager, runtime);
        }
        return container;
    }
}
