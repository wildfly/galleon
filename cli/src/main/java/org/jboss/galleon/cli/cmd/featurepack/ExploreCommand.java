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
package org.jboss.galleon.cli.cmd.featurepack;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "explore", description = "Explore a feature-pack", activator = NoStateCommandActivator.class)
public class ExploreCommand extends AbstractFeaturePackCommand {

    @Override
    protected void runCommand(PmCommandInvocation commandInvocation) throws CommandExecutionException {
        if (fpl != null && file != null) {
            throw new CommandExecutionException("File and location can't be both set");
        }
        PmSession session = commandInvocation.getPmSession();
        if (session.getContainer() != null) {
            throw new CommandExecutionException("Already entered, use leave command");
        }

        String prompt = null;
        String name = null;
        FeatureContainer container;
        FeaturePackLocation loc = null;
        try {
            if (fpl != null) {
                name = fpl;
                loc = session.getResolvedLocation(fpl);
            } else {
                loc = session.getLayoutFactory().addLocal(file.toPath(), true);
                name = loc.getProducerName() + FeaturePackLocation.CHANNEL_START
                        + loc.getChannelName() + FeaturePackLocation.BUILD_START + loc.getBuild();
            }
            container = FeatureContainers.fromFeaturePackId(session,
                    session.newProvisioningManager(null, false), loc.getFPID(), name);

            session.setExploredContainer(container);
            prompt = name + PathParser.PATH_SEPARATOR;
            session.setCurrentPath(FeatureContainerPathConsumer.ROOT);
        } catch (Exception ex) {
            if (ex instanceof CommandExecutionException) {
                throw (CommandExecutionException) ex;
            }
            throw new CommandExecutionException("Stream resolution failed", ex);
        }
        commandInvocation.setPrompt(PmSession.buildPrompt(prompt));
        commandInvocation.println("Exploring " + name + ". Use 'state leave' to leave exploration.");
    }
}
