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
package org.jboss.galleon.cli.cmd.mvn;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GavCompleter;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import static org.jboss.galleon.cli.AbstractFeaturePackCommand.FP_OPTION_NAME;
import org.jboss.galleon.cli.StreamCompleter;
import org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand.FPActivator;
import org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand.StreamNameActivator;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisioningCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "fetch-feature-pack", description = "download a feature-pack to the local repository")
public class FetchArtifact extends PmSessionCommand {

    @Argument(completer = StreamCompleter.class, activator = StreamNameActivator.class)
    protected String streamName;

    @Option(name = FP_OPTION_NAME, completer = GavCompleter.class, activator = FPActivator.class)
    protected String fpCoords;

    @Option(hasValue = false)
    private boolean verbose;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            Gav gav = AbstractFPProvisioningCommand.getGav(fpCoords, streamName, session.getPmSession());
            if (verbose) {
                session.getPmSession().enableMavenTrace(true);
            }
            try {
                session.getPmSession().getArtifactResolver().resolve(gav.toArtifactCoords());
                session.println("artifact installed in local mvn repository " + session.getPmSession().
                        getPmConfiguration().getMavenConfig().getLocalRepository());
            } finally {
                session.getPmSession().enableMavenTrace(false);
            }
        } catch (ArtifactException ex) {
            throw new CommandExecutionException(ex);
        }
    }

}
