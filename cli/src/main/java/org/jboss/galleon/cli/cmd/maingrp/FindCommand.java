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
package org.jboss.galleon.cli.cmd.maingrp;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.UniverseManager.UniverseVisitor;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "find", description = HelpDescriptions.FIND)
public class FindCommand extends PmSessionCommand {

    @Argument(description = HelpDescriptions.FIND_PATTERN)
    private String pattern;

    @Option(required = false, name = "universe", description = HelpDescriptions.FIND_UNIVERSE)
    private String fromUniverse;

    @Option(required = false, name = "resolved-only", hasValue = false, description = HelpDescriptions.FIND_RESOLVED_ONLY)
    private Boolean resolvedOnly;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (pattern == null) {
            throw new CommandExecutionException(CliErrors.missingPattern());
        } else {
            Map<UniverseSpec, Set<FeaturePackLocation>> results = new HashMap<>();
            Map<UniverseSpec, Exception> exceptions = new HashMap<>();
            if (!pattern.endsWith("*")) {
                pattern = pattern + "*";
            }
            pattern = pattern.replaceAll("\\*", ".*");
            boolean containsFrequency = pattern.contains("" + FeaturePackLocation.FREQUENCY_START);
            Pattern compiledPattern = Pattern.compile(pattern);
            Integer[] numResults = new Integer[1];
            numResults[0] = 0;
            try {
                Comparator<FeaturePackLocation> locComparator = new Comparator<FeaturePackLocation>() {
                    @Override
                    public int compare(FeaturePackLocation o1, FeaturePackLocation o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                };
                UniverseVisitor visitor = new UniverseVisitor() {
                    @Override
                    public void visit(Producer<?> producer, FeaturePackLocation loc) {
                        try {
                            if (resolvedOnly && !producer.getChannel(loc.getChannelName()).isResolved(loc)) {
                                return;
                            }
                        } catch (ProvisioningException ex) {
                            exception(loc.getUniverse(), ex);
                            return;
                        }

                        // Universe could have been set in the pattern, matches on
                        // the canonical and exposed (named universe).
                        FeaturePackLocation exposedLoc = invoc.getPmSession().
                                getExposedLocation(null, loc);
                        boolean canonicalMatch = compiledPattern.matcher(loc.toString()).matches();
                        boolean exposedMatch = compiledPattern.matcher(exposedLoc.toString()).matches();
                        // Frequency has been set, only matches FPL that contains a frequency.
                        // If no frequency set, matches FPL that don't contain a frequency.
                        if (canonicalMatch || exposedMatch) {
                            if ((containsFrequency && loc.getFrequency() != null)
                                    || (!containsFrequency && loc.getFrequency() == null)) {
                                Set<FeaturePackLocation> locations = results.get(loc.getUniverse());
                                if (locations == null) {
                                    locations = new TreeSet<>(locComparator);
                                    results.put(loc.getUniverse(), locations);
                                }
                                if (exposedMatch) {
                                    locations.add(exposedLoc);
                                } else {
                                    locations.add(loc);
                                }
                                numResults[0] = numResults[0] + 1;
                            }
                        }
                    }

                    @Override
                    public void exception(UniverseSpec spec, Exception ex) {
                        exceptions.put(spec, ex);
                    }
                };

                if (fromUniverse == null) {
                    invoc.getPmSession().getUniverse().visitAllUniverses(visitor,
                            true);
                } else {
                    invoc.getPmSession().getUniverse().visitUniverse(UniverseSpec.
                            fromString(fromUniverse), visitor, true);
                }

                invoc.println(Config.getLineSeparator() + "Found "
                        + numResults[0] + " feature pack locations.");

                printExceptions(invoc, exceptions);

                for (Entry<UniverseSpec, Set<FeaturePackLocation>> entry : results.entrySet()) {
                    UniverseSpec universeSpec = entry.getKey();
                    String universeName = invoc.getPmSession().getUniverse().
                            getUniverseName(null, universeSpec);
                    universeName = universeName == null ? universeSpec.toString() : universeName;
                    invoc.println(Config.getLineSeparator() + "Universe "
                            + universeName
                            + Config.getLineSeparator());
                    for (FeaturePackLocation loc : entry.getValue()) {
                        invoc.println(loc.toString());
                    }
                }
            } catch (ProvisioningException ex) {
                throw new CommandExecutionException(ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }

    public static void printExceptions(PmCommandInvocation invoc,
            Map<UniverseSpec, Exception> exceptions) {
        if (!exceptions.isEmpty()) {
            invoc.println("Some exceptions occured while accessing universes:");
        }
        for (Entry<UniverseSpec, Exception> entry : exceptions.entrySet()) {
            invoc.println(Config.getLineSeparator()
                    + "Exception for "
                    + exceptions.get(entry.getKey()).getMessage()
                    + Config.getLineSeparator());
        }
    }

}
