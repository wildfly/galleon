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
package org.jboss.galleon.cli.cmd.universe;

import java.util.Collection;
import java.util.Set;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1UniverseFactory;
import org.jboss.galleon.universe.maven.MavenChannel;
import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenUniverse;
import org.jboss.galleon.universe.maven.MavenUniverseException;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "list", description = "List universes and products")
public class UniverseListCommand implements Command<PmCommandInvocation> {

    @Override
    public CommandResult execute(PmCommandInvocation commandInvocation)
            throws CommandException, InterruptedException {
        UniverseSpec defaultUniverse = commandInvocation.getPmSession().getUniverse().
                getDefaultUniverseSpec();
        try {
            UniverseSpec builtinUniverse = commandInvocation.getPmSession().
                    getUniverse().getBuiltinUniverseSpec();
            if (builtinUniverse.equals(defaultUniverse)) {
                commandInvocation.println("Default universe (builtin maven universe)");
                printUniverse(builtinUniverse, commandInvocation);
            } else if (defaultUniverse != null) {
                commandInvocation.println("Default universe");
                printUniverse(defaultUniverse, commandInvocation);
            }

        } catch (ProvisioningException ex) {
            commandInvocation.println("Exception retrieving default universe " + ex);
        }

        Set<String> universes = commandInvocation.getPmSession().getUniverse().getUniverseNames();
        if (!universes.isEmpty()) {
            commandInvocation.println(Config.getLineSeparator() + "Universes local to this provisioning state");
        }
        for (String u : universes) {
            UniverseSpec universe = null;
            try {
                universe = commandInvocation.getPmSession().getUniverse().getUniverseSpec(u);
                if (universe.getFactory().equals(LegacyGalleon1UniverseFactory.ID)) {
                    continue;
                }
                printUniverse(universe, commandInvocation);
            } catch (ProvisioningException ex) {
                commandInvocation.println("Exception " + ex.getLocalizedMessage()
                        + " retrieving universe " + u);
            }
        }

        return CommandResult.SUCCESS;
    }

    private void printUniverse(UniverseSpec spec, PmCommandInvocation invoc) throws ProvisioningException {
        UniverseResolver resolver = invoc.getPmSession().getUniverse().getUniverseResolver();
        org.jboss.galleon.universe.Universe universe = resolver.getUniverse(spec);
        invoc.println(spec.toString() + (spec.getLocation().equals(universe.getLocation()) ? ""
                : ", actual location " + universe.getLocation()));
        Collection<MavenProducer> producers = universe.getProducers();
        if (producers.isEmpty()) {
            invoc.println(" No product available");
        } else if (universe instanceof MavenUniverse) {
            printMavenUniverse(spec, (MavenUniverse) universe, invoc);
        } else {
            printGenericUniverse(spec, universe, invoc);
        }
    }

    private void printMavenUniverse(UniverseSpec spec, MavenUniverse universe,
            PmCommandInvocation invoc) throws MavenUniverseException {
        for (MavenProducer producer : universe.getProducers()) {
            invoc.println(" Product: " + producer.getName() + ", artifact "
                    + producer.getFeaturePackGroupId() + ":"
                    + producer.getFeaturePackArtifactId());
            invoc.println("   Releases ");
            for (MavenChannel channel : producer.getChannels()) {
                for (String freq : channel.getFrequencies()) {
                    invoc.println("    " + producer.getName() + ":"
                            + channel.getName() + "/" + freq + ", version range "
                            + channel.getVersionRange());
                }
            }
        }
    }

    private void printGenericUniverse(UniverseSpec spec, org.jboss.galleon.universe.Universe<?> universe,
            PmCommandInvocation invoc) throws ProvisioningException {
        for (Producer<?> producer : universe.getProducers()) {
            invoc.println("  Product: " + producer.getName());
            invoc.println("    Releases ");
            for (Channel channel : producer.getChannels()) {
                invoc.println("    " + producer.getName() + ":" + channel.getName());
            }
        }
    }

}
