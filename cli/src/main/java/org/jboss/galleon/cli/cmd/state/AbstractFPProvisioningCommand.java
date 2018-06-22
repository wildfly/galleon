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

import java.io.IOException;
import static org.jboss.galleon.cli.AbstractFeaturePackCommand.FP_OPTION_NAME;

import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GavCompleter;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.StreamCompleter;
import org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand.FPActivator;
import org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand.StreamNameActivator;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFPProvisioningCommand extends AbstractStateCommand {

    @Argument(completer = StreamCompleter.class, activator = StreamNameActivator.class)
    protected String streamName;

    @Option(name = FP_OPTION_NAME, completer = GavCompleter.class, activator = FPActivator.class)
    protected String fpCoords;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State session) throws IOException, ProvisioningException, CommandExecutionException {
        ArtifactCoords.Gav gav = getGav(fpCoords, streamName, invoc.getPmSession());
        FeaturePackLocation fpl = LegacyGalleon1Universe.toFpl(gav);
        FPID fpid = fpl.getFPID();
        if (!invoc.getPmSession().existsInLocalRepository(fpid)) {
            try {
                invoc.getPmSession().downloadFp(fpid);
            } catch (ArtifactException ex) {
                throw new CommandExecutionException(ex);
            }
        }
        runCommand(invoc, session, fpl);
    }

    protected abstract void runCommand(PmCommandInvocation invoc, State session, FeaturePackLocation fpl) throws IOException, ProvisioningException, CommandExecutionException;

    public static ArtifactCoords.Gav getGav(String fpCoords, String streamName, PmSession session) throws CommandExecutionException {
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
