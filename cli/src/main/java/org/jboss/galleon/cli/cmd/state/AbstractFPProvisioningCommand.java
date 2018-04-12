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
package org.jboss.galleon.cli.cmd.state;

import static org.jboss.galleon.cli.ProvisioningFeaturePackCommand.FP_OPTION_NAME;

import org.aesh.command.activator.OptionActivator;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GavCompleter;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.StreamCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFPProvisioningCommand extends AbstractStateCommand {

    public static class FPActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            String argumentValue = parsedCommand.argument().value();
            return argumentValue == null;
        }
    }

    public static class StreamNameActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(FP_OPTION_NAME);
            return opt == null || opt.value() == null;
        }
    }

    @Argument(completer = StreamCompleter.class, activator = StreamNameActivator.class)
    protected String streamName;

    @Option(name = FP_OPTION_NAME, completer = GavCompleter.class, activator = FPActivator.class)
    protected String fpCoords;

    public ArtifactCoords.Gav getGav(PmSession session) throws CommandExecutionException {
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

    public String getName() {
        if (streamName == null) {
            return fpCoords;
        } else {
            return streamName;
        }
    }
}
