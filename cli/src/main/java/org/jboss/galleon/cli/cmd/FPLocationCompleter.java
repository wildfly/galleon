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
package org.jboss.galleon.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aesh.command.completer.OptionCompleter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.UniverseManager;
import org.jboss.galleon.model.GaecRange;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1UniverseFactory;

/**
 *
 * @author jdenise@redhat.com
 */
public class FPLocationCompleter implements OptionCompleter<PmCompleterInvocation> {

    @Override
    public void complete(PmCompleterInvocation completerInvocation) {
        try {
            doComplete(completerInvocation);
        } catch (ProvisioningException ex) {
            // no completion.
            Logger.getLogger(FPLocationCompleter.class.getName()).log(Level.FINEST,
                    "Exception while completing: {0}", ex.getLocalizedMessage());
        }
    }

    private void doComplete(PmCompleterInvocation completerInvocation) throws ProvisioningException {
        // Legacy completer first
        PmSession pmSession = completerInvocation.getPmSession();
        UniverseManager resolver = pmSession.getUniverse();
        UniverseSpec defaultUniverse = pmSession.getUniverse().getDefaultUniverseSpec();
        Set<String> aliases = pmSession.getUniverse().getUniverseNames();
        //producer[@universe]:channel/frequency#build
        //producer[@factory-id/location]:channel/frequency#build
        String buffer = completerInvocation.getGivenCompleteValue();

        List<String> candidates = new ArrayList<>();
        FPLocationParser.ParsedFPLocation loc = null;
        try {
            if (buffer.isEmpty()) {
                if (defaultUniverse != null) {
                    getAllProducers(null, defaultUniverse, resolver.getUniverse(defaultUniverse), candidates);
                }
                for (String name : aliases) {
                    UniverseSpec u = pmSession.getUniverse().getUniverseSpec(name);
                    if (u.getFactory().equals(LegacyGalleon1UniverseFactory.ID)) {
                        continue;
                    }
                    if (!u.equals(defaultUniverse)) {
                        getAllProducers(u.toString(), u, resolver.getUniverse(u), candidates);
                    }
                }
            } else {
                loc = FPLocationParser.parse(buffer, new FPLocationParser.FPLocationCompletionConsumer() {
                    @Override
                    public void completeProducer(String producer) throws FPLocationParserException, ProvisioningException {
                        // Lookup in all universes for a producer, we don't know the universe yet
                        if (defaultUniverse != null) {
                            getProducers(producer, null, resolver.getUniverse(defaultUniverse), candidates);

                        }
                        for (String name : aliases) {
                            UniverseSpec u = pmSession.getUniverse().getUniverseSpec(name);
                            if (!u.equals(defaultUniverse)) {
                                getProducers(producer, name, resolver.getUniverse(u), candidates);

                            }
                        }
                    }

                    @Override
                    public void completeUniverse(FPLocationParser.ParsedFPLocation parsedLocation, String universe) throws FPLocationParserException, ProvisioningException {
                        for (String name : aliases) {
                            UniverseSpec spec = pmSession.getUniverse().getUniverseSpec(name);
                            if (spec != null && resolver.getUniverse(spec).hasProducer(parsedLocation.getProducer())) {
                                if (name.equals(universe)) {
                                    candidates.add(name + FeaturePackLocation.CHANNEL_START);
                                } else if (name.startsWith(universe)) {
                                    candidates.add(name);
                                }
                            }
                        }
                    }

                    @Override
                    public void completeUniverseLocation(FPLocationParser.ParsedFPLocation parsedLocation, String universeLocation) throws FPLocationParserException, ProvisioningException {
                        for (String name : aliases) {
                            UniverseSpec spec = pmSession.getUniverse().getUniverseSpec(name);
                            if (spec == null || spec.getFactory().equals(LegacyGalleon1UniverseFactory.ID)) {
                                continue;
                            }
                            final String specLocationString = spec.getLocation().toString();
                            if (!candidates.contains(specLocationString)) {
                                if (spec.getFactory().equals(parsedLocation.getUniverseFactory())
                                        && resolver.getUniverse(spec).hasProducer(parsedLocation.getProducer())) {
                                    if (specLocationString.equals(universeLocation)) {
                                        candidates.add(specLocationString + FeaturePackLocation.UNIVERSE_LOCATION_END);
                                    } else if (specLocationString.startsWith(universeLocation)) {
                                        candidates.add(specLocationString);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void completeChannel(FPLocationParser.ParsedFPLocation parsedLocation, String channel) throws FPLocationParserException, ProvisioningException {
                        Producer<?> p = getProducer(parsedLocation, pmSession);
                        if (p == null) {
                            return;
                        }
                        for (Channel c : p.getChannels()) {
                            if (c.getName().equals(channel)) {
                                // Do nothing, do not inline separators. Separators are to be added explicitly
                                // this could be revisited.
                                candidates.add(channel);
                            } else if (c.getName().startsWith(channel)) {
                                candidates.add(c.getName());
                            }
                        }
                    }

                    @Override
                    public void completeFrequency(FPLocationParser.ParsedFPLocation parsedLocation, String frequency) throws FPLocationParserException, ProvisioningException {
                        Producer<?> p = getProducer(parsedLocation, pmSession);
                        if (p == null) {
                            return;
                        }
                        for (String freq : p.getFrequencies()) {
                            if (freq.equals(frequency)) {
                                // Do not inline the build separator, separator is to be added explicitly
                                // this could be revisited.
                                candidates.add(freq);
                            } else if (freq.startsWith(frequency)) {
                                candidates.add(freq);
                            }
                        }
                    }

                    @Override
                    public void completeChannelSeparator(FPLocationParser.ParsedFPLocation parsedLocation) throws FPLocationParserException, ProvisioningException {
                        candidates.add("" + FeaturePackLocation.CHANNEL_START);
                    }

                    @Override
                    public void completeBuild(FPLocationParser.ParsedFPLocation parsedLocation, String build) throws FPLocationParserException, ProvisioningException {
                        UniverseSpec spec = null;
                        if (parsedLocation.getUniverseName() != null) {
                            spec = pmSession.getUniverse().getUniverseSpec(parsedLocation.getUniverseName());
                        } else if (parsedLocation.getUniverseFactory() == null) {
                            spec = pmSession.getUniverse().getDefaultUniverseSpec();
                        } else {
                            spec = new UniverseSpec(parsedLocation.getUniverseFactory(), GaecRange.parse(parsedLocation.getUniverseLocation()));
                        }
                        if (spec != null) {
                            String latestBuild = null;
                            // FPID
                            if (parsedLocation.getFrequency() == null) {
                                FeaturePackLocation.FPID id = new FeaturePackLocation(spec, parsedLocation.getProducer(),
                                        parsedLocation.getChannel(), null, null).getFPID();
                                latestBuild = pmSession.getUniverse().getUniverse(spec).
                                        getProducer(parsedLocation.getProducer()).getChannel(parsedLocation.getChannel()).getLatestBuild(id);
                            } else {
                                FeaturePackLocation loc = new FeaturePackLocation(spec, parsedLocation.getProducer(),
                                        parsedLocation.getChannel(), parsedLocation.getFrequency(), null);
                                latestBuild = pmSession.getUniverse().getUniverse(spec).
                                        getProducer(parsedLocation.getProducer()).getChannel(parsedLocation.getChannel()).getLatestBuild(loc);
                            }
                            if (latestBuild != null) {
                                if (latestBuild.startsWith(build)) {
                                    candidates.add(latestBuild);
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Logger.getLogger(FPLocationCompleter.class.getName()).log(Level.FINEST,
                    "Exception while completing: {0}", ex.getLocalizedMessage());
            return;
        }
        completerInvocation.addAllCompleterValues(candidates);
        if (candidates.size() == 1) {
            if (completerInvocation.getGivenCompleteValue().endsWith(candidates.get(0))) {
                completerInvocation.setAppendSpace(true);
            } else {
                completerInvocation.setAppendSpace(false);
            }
            completerInvocation.setOffset(completerInvocation.getGivenCompleteValue().length() - (loc == null ? 0 : (loc.getMarker() + 1)));
        }

    }

    private Producer<?> getProducer(FPLocationParser.ParsedFPLocation parsedLocation, PmSession pmSession) {
        try {
            UniverseSpec spec = null;
            if (parsedLocation.getUniverseName() != null) {
                spec = pmSession.getUniverse().getUniverseSpec(parsedLocation.getUniverseName());
            } else if (parsedLocation.getUniverseFactory() == null) {
                // default universe
                spec = pmSession.getUniverse().getDefaultUniverseSpec();
            } else {
                spec = new UniverseSpec(parsedLocation.getUniverseFactory(), GaecRange.parse(parsedLocation.getUniverseLocation()));
            }
            if (spec == null) {
                return null;
            }
            Universe<?> universe = pmSession.getUniverse().getUniverse(spec);
            if (universe == null) {
                return null;
            }
            for (Producer<?> p : universe.getProducers()) {
                if (p.getName().equals(parsedLocation.getProducer())) {
                    return p;
                }
            }
        } catch (ProvisioningException ex) {
            Logger.getLogger(FPLocationCompleter.class.getName()).log(Level.FINEST,
                    "Exception while completing: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

    private void getAllProducers(String name, UniverseSpec spec, Universe<?> universe, List<String> candidates) throws ProvisioningException {
        for (Producer<?> p : universe.getProducers()) {
            for (Channel c : p.getChannels()) {
                if (!candidates.contains(p.getName())) {
                    candidates.add(p.getName() + FeaturePackLocation.CHANNEL_START + c.getName());
                }
            }
        }
    }

    private void getProducers(String producerName, String universeName, Universe<?> universe, List<String> candidates) throws ProvisioningException {
        for (Producer<?> p : universe.getProducers()) {
            if (!candidates.contains(p.getName())) {
                // Display producer:channel as a whole, makes it clear that we require both.
                if (p.getName().startsWith(producerName)) {
                    if (universeName == null) {
                        for (Channel c : p.getChannels()) {
                            candidates.add(p.getName() + FeaturePackLocation.CHANNEL_START + c.getName());
                        }
                    } else {
                        for (Channel c : p.getChannels()) {
                            candidates.add(p.getName() + FeaturePackLocation.UNIVERSE_START
                                    + universeName + FeaturePackLocation.CHANNEL_START + c.getName());
                        }
                    }
                }
            }
        }
    }

}
