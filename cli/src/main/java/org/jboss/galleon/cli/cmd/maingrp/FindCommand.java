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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.tracking.ProgressTrackers;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "find", description = HelpDescriptions.FIND)
public class FindCommand extends PmSessionCommand {

    private static class Result {

        private final FeaturePackLocation location;
        private final Set<ConfigId> layers;

        Result(FeaturePackLocation location) {
            layers = new HashSet<>();
            this.location = location;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(location.toString());
            if (!layers.isEmpty()) {
                builder.append(" layers[ ");
                for (ConfigId l : layers) {
                    builder.append(l.getModel()).append("/").append(l.getName()).append(" ");
                }
                builder.append("]");
            }
            return builder.toString();
        }
    }
    @Argument(description = HelpDescriptions.FIND_PATTERN)
    private String pattern;

    @Option(description = HelpDescriptions.FIND_LAYERS_PATTERN, name = "layers")
    private String layerPattern;

    @Option(required = false, name = "universe", description = HelpDescriptions.FIND_UNIVERSE)
    private String fromUniverse;

    @Option(required = false, name = "resolved-only", hasValue = false, description = HelpDescriptions.FIND_RESOLVED_ONLY)
    private Boolean resolvedOnly;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        if (pattern == null && layerPattern == null) {
            throw new CommandExecutionException(CliErrors.missingPattern());
        } else {
            if (pattern == null) {
                pattern = ".Final";
            }
            Map<UniverseSpec, Set<Result>> results = new HashMap<>();
            Map<UniverseSpec, Set<String>> exceptions = new HashMap<>();
            if (!pattern.endsWith("*")) {
                pattern = pattern + "*";
            }
            pattern = pattern.replaceAll("\\*", ".*");
            List<Pattern> layersCompiledPatterns = new ArrayList<>();
            if (layerPattern != null) {
                for (String l : layerPattern.split(",")) {
                    if (!l.endsWith("*")) {
                        l = l + "*";
                    }
                    l = l.replaceAll("\\*", ".*");
                    layersCompiledPatterns.add(Pattern.compile(l));
                }
            }
            boolean containsFrequency = pattern.contains("" + FeaturePackLocation.FREQUENCY_START);
            Pattern compiledPattern = Pattern.compile(pattern);

            Integer[] numResults = new Integer[1];
            numResults[0] = 0;
            ProgressTracker<FPID> track = null;
            if (invoc.getPmSession().isTrackersEnabled()) {
                track = ProgressTrackers.newFindTracker(invoc);
            }
            ProgressTracker<FPID> tracker = track;
            invoc.getPmSession().unregisterTrackers();

            // Search for an installation in the context
            Path installation = null;
            try {
                installation = Util.lookupInstallationDir(invoc.getConfiguration().getAeshContext(), null);
            } catch (ProvisioningException ex) {
                // XXX OK, no installation.
            }
            Path finalPath = installation;
            try {
                Comparator<Result> locComparator = new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.location.toString().compareTo(o2.location.toString());
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
                        if (tracker != null) {
                            tracker.processing(loc.getFPID());
                        }
                        // Universe could have been set in the pattern, matches on
                        // the canonical and exposed (named universe).
                        FeaturePackLocation exposedLoc = invoc.getPmSession().
                                getExposedLocation(finalPath, loc);
                        boolean canonicalMatch = compiledPattern.matcher(loc.toString()).matches();
                        boolean exposedMatch = compiledPattern.matcher(exposedLoc.toString()).matches();
                        // Frequency has been set, only matches FPL that contains a frequency.
                        // If no frequency set, matches FPL that don't contain a frequency.
                        if (canonicalMatch || exposedMatch) {
                            if ((containsFrequency && loc.getFrequency() != null)
                                    || (!containsFrequency && loc.getFrequency() == null)) {
                                Result result;
                                if (exposedMatch) {
                                    result = new Result(exposedLoc);
                                } else {
                                    result = new Result(loc);
                                }
                                if (!layersCompiledPatterns.isEmpty()) {
                                    try {
                                        FeaturePackConfig config = FeaturePackConfig.forLocation(loc);
                                        ProvisioningConfig provisioning = ProvisioningConfig.builder().addFeaturePackDep(config).build();
                                        Set<ConfigId> layers = new HashSet<>();
                                        try (ProvisioningLayout<FeaturePackLayout> layout
                                                = invoc.getPmSession().getLayoutFactory().newConfigLayout(provisioning)) {
                                            for (FeaturePackLayout l : layout.getOrderedFeaturePacks()) {
                                                layers.addAll(l.loadLayers());
                                            }
                                        }
                                        for (ConfigId l : layers) {
                                            for (Pattern p : layersCompiledPatterns) {
                                                if (p.matcher(l.getName()).matches()) {
                                                    result.layers.add(l);
                                                }
                                            }
                                        }
                                        if (!result.layers.isEmpty()) {
                                            Set<Result> locations = results.get(loc.getUniverse());
                                            if (locations == null) {
                                                locations = new TreeSet<>(locComparator);
                                                results.put(loc.getUniverse(), locations);
                                            }
                                            locations.add(result);
                                            numResults[0] = numResults[0] + 1;
                                        }
                                    } catch (IOException | ProvisioningException ex) {
                                        exception(loc.getUniverse(), ex);
                                    }
                                } else {
                                    Set<Result> locations = results.get(loc.getUniverse());
                                    if (locations == null) {
                                        locations = new TreeSet<>(locComparator);
                                        results.put(loc.getUniverse(), locations);
                                    }
                                    locations.add(result);
                                    numResults[0] = numResults[0] + 1;
                                }

                            }
                        }
                    }

                    @Override
                    public void exception(UniverseSpec spec, Exception ex) {
                        Set<String> set = exceptions.get(spec);
                        if (set == null) {
                            set = new HashSet<>();
                            exceptions.put(spec, set);
                        }
                        set.add(ex.getLocalizedMessage() == null
                                ? ex.getMessage() : ex.getLocalizedMessage());
                    }
                };
                if (tracker != null) {
                    tracker.starting(-1);
                }

                if (fromUniverse == null) {
                    invoc.getPmSession().getUniverse().visitAllUniverses(visitor,
                            true, finalPath);
                } else {
                    invoc.getPmSession().getUniverse().visitUniverse(UniverseSpec.
                            fromString(fromUniverse), visitor, true);
                }
                if (tracker != null) {
                    tracker.complete();
                }

                printExceptions(invoc, exceptions);

                invoc.println(Config.getLineSeparator() + "Found "
                        + numResults[0] + " feature pack location" + (numResults[0] > 1 ? "s." : "."));
                for (Entry<UniverseSpec, Set<Result>> entry : results.entrySet()) {
                    for (Result loc : entry.getValue()) {
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
            Map<UniverseSpec, Set<String>> exceptions) {
        if (!exceptions.isEmpty()) {
            invoc.println("Some exceptions occured while accessing universes.");
        }
        for (Entry<UniverseSpec, Set<String>> entry : exceptions.entrySet()) {
            for (String ex : entry.getValue()) {
                invoc.println(ex + " in " + entry.getKey());
            }
        }
    }

}
