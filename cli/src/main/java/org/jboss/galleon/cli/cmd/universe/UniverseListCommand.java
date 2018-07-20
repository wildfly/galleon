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
import java.util.regex.Pattern;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.UniverseManager;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1UniverseFactory;
import org.jboss.galleon.universe.maven.MavenProducer;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "list", description = "List universes and products")
public class UniverseListCommand extends PmSessionCommand {

    @Option(required = false, name = "product", description = "Select products that match the provided pattern.")
    private String product;

    @Option(required = false, name = "universe", description = "References a not installed universe")
    private String universe;

    @Override
    public void runCommand(PmCommandInvocation commandInvocation)
            throws CommandExecutionException {
        UniverseSpec defaultUniverse = commandInvocation.getPmSession().getUniverse().
                getDefaultUniverseSpec();

        Pattern cPattern = null;
        if (product != null) {
            product = product.replaceAll("\\*", ".*");
            cPattern = Pattern.compile(product);
        }
        if (universe != null) {
            int locIndex = universe.indexOf("(");
            if (locIndex < 0) {
                throw new CommandExecutionException(CliErrors.invalidUniverse());
            }
            int locIndexEnd = universe.indexOf(")");
            if (locIndexEnd < 0) {
                throw new CommandExecutionException(CliErrors.invalidUniverse());
            }

            String factory = universe.substring(0, locIndex);
            String location = universe.substring(locIndex + 1, locIndexEnd);
            try {
                UniverseSpec spec = new UniverseSpec(factory, location);
                System.out.println("SPEC " + spec);
                printUniverse(cPattern, spec, commandInvocation);
            } catch (ProvisioningException ex) {
                throw new CommandExecutionException(commandInvocation.getPmSession(),
                        CliErrors.resolvedUniverseFailed(), ex);
            }
        } else {
            try {
                UniverseSpec builtinUniverse = commandInvocation.getPmSession().
                        getUniverse().getBuiltinUniverseSpec();
                if (builtinUniverse.equals(defaultUniverse)) {
                    commandInvocation.println("Default universe (builtin maven universe)");
                    printUniverse(cPattern, builtinUniverse, commandInvocation);
                } else if (defaultUniverse != null) {
                    commandInvocation.println("Default universe");
                    printUniverse(cPattern, defaultUniverse, commandInvocation);
                }
            } catch (ProvisioningException ex) {
                throw new CommandExecutionException(commandInvocation.getPmSession(),
                        CliErrors.resolvedUniverseFailed(), ex);
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
                    printUniverse(cPattern, universe, commandInvocation);
                } catch (ProvisioningException ex) {
                    throw new CommandExecutionException(commandInvocation.getPmSession(),
                            CliErrors.resolvedUniverseFailed(), ex);
                }
            }
        }
    }

    private static void printUniverse(Pattern cPattern, UniverseSpec spec, PmCommandInvocation invoc) throws ProvisioningException {
        UniverseManager resolver = invoc.getPmSession().getUniverse();
        org.jboss.galleon.universe.Universe universe = resolver.getUniverse(spec);
        invoc.println(spec.toString() + (spec.getLocation().equals(universe.getLocation()) ? ""
                : ", actual location " + universe.getLocation()));
        Collection<MavenProducer> producers = universe.getProducers();
        if (producers.isEmpty()) {
            invoc.println(" No product available");
        } else {
            printUniverse(cPattern, spec, universe, invoc);
        }
    }

    private static void printUniverse(Pattern cPattern, UniverseSpec spec, org.jboss.galleon.universe.Universe<?> universe,
            PmCommandInvocation invoc) throws ProvisioningException {
        Table table = new Table(Headers.PRODUCT, Headers.VERSION, Headers.QUALIFIER, Headers.BUILD);
        for (Producer<?> producer : universe.getProducers()) {
            if (cPattern == null || cPattern.matcher(producer.getName()).matches()) {
                for (Channel channel : producer.getChannels()) {
                    for (String freq : producer.getFrequencies()) {
                        String build = getBuild(spec, producer, channel, freq);
                        table.addLine(producer.getName(), channel.getName(), freq,
                                (build == null ? "" : build));
                    }
                }
            }
        }
        if (table.isEmpty()) {
            invoc.println(" No product found.");
        } else {
            table.sort(Table.SortType.ASCENDANT);
            invoc.println(table.build());
        }
    }

    private static String getBuild(UniverseSpec spec, Producer<?> producer, Channel channel, String freq) {
        FeaturePackLocation loc = new FeaturePackLocation(spec, producer.getName(), channel.getName(), freq, null);
        String build = null;
        try {
            build = channel.getLatestBuild(loc);
        } catch (ProvisioningException ex) {
            // OK, no build.
        }
        return build;
    }


}
