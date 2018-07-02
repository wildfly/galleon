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

import org.aesh.command.option.Argument;
import org.aesh.utils.Config;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFPProvisioningCommand extends AbstractStateCommand {

    @Argument(completer = FPLocationCompleter.class, required = true)
    protected String streamName;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State session) throws IOException, ProvisioningException, CommandExecutionException {
        FeaturePackLocation fpl = invoc.getPmSession().getResolvedLocation(streamName);
        if (!invoc.getPmSession().existsInLocalRepository(fpl.getFPID())) {
            try {
                invoc.getPmSession().println(Config.getLineSeparator() + "retrieving feature-pack content from remote repository...");
                invoc.getPmSession().downloadFp(fpl.getFPID());
            } catch (ArtifactException ex) {
                throw new CommandExecutionException(ex);
            }
        }
        runCommand(invoc, session, fpl);
    }

    protected abstract void runCommand(PmCommandInvocation invoc, State session, FeaturePackLocation fpl) throws IOException, ProvisioningException, CommandExecutionException;

    public String getName() {
        return streamName;
    }
}
