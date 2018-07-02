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

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class FPLocationParser {

    public interface FPLocationCompletionConsumer {

        void completeProducer(String producer) throws FPLocationParserException, ProvisioningException;

        void completeUniverse(ParsedFPLocation parsedLocation, String universeFactory) throws FPLocationParserException, ProvisioningException;

        void completeUniverseLocation(ParsedFPLocation parsedLocation, String universeLocation) throws FPLocationParserException, ProvisioningException;

        void completeChannel(ParsedFPLocation parsedLocation, String channel) throws FPLocationParserException, ProvisioningException;

        void completeFrequency(ParsedFPLocation parsedLocation, String frequency) throws FPLocationParserException, ProvisioningException;

        void completeChannelSeparator(ParsedFPLocation parsedLocation) throws FPLocationParserException, ProvisioningException;

        void completeBuild(ParsedFPLocation parsedLocation, String build) throws FPLocationParserException, ProvisioningException;

    }

    public static class ParsedFPLocation {

        private String producer;
        private String universeName;
        private String universeFactory;
        private String universeLocation;
        private String channel;
        private String frequency;
        private String build;
        private int marker = -1;
        /**
         * @return the producer
         */
        public String getProducer() {
            return producer;
        }

        public int getMarker() {
            return marker;
        }

        /**
         * @return the universeName
         */
        public String getUniverseName() {
            return universeName;
        }

        /**
         * @return the universeFactory
         */
        public String getUniverseFactory() {
            return universeFactory;
        }

        /**
         * @return the universeLocation
         */
        public String getUniverseLocation() {
            return universeLocation;
        }

        /**
         * @return the channel
         */
        public String getChannel() {
            return channel;
        }

        /**
         * @return the frequency
         */
        public String getFrequency() {
            return frequency;
        }

        /**
         * @return the build
         */
        public String getBuild() {
            return build;
        }
    }

    enum State {
        PRODUCER,
        UNIVERSE,
        UNIVERSE_LOCATION,
        CHANNEL_SEPARATOR,
        CHANNEL,
        FREQUENCY,
        BUILD
    }

    public static ParsedFPLocation parse(String location, FPLocationCompletionConsumer consumer) throws FPLocationParserException, ProvisioningException {

        State state = State.PRODUCER;
        char[] arr = location.toCharArray();
        int offset = 0;
        ParsedFPLocation parsedLocation = new ParsedFPLocation();
        StringBuilder builder = new StringBuilder();
        while (offset < arr.length) {
            char c = arr[offset];
            offset += 1;
            switch (state) {
                case PRODUCER: {
                    if (c == FeaturePackLocation.UNIVERSE_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.producer = builder.toString();
                        builder = new StringBuilder();
                        state = State.UNIVERSE;
                    } else if (c == FeaturePackLocation.CHANNEL_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.producer = builder.toString();
                        builder = new StringBuilder();
                        state = State.CHANNEL;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case UNIVERSE: {
                    if (c == FeaturePackLocation.UNIVERSE_LOCATION_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.universeFactory = builder.toString();
                        builder = new StringBuilder();
                        state = State.UNIVERSE_LOCATION;
                    } else if (c == FeaturePackLocation.CHANNEL_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.universeName = builder.toString();
                        builder = new StringBuilder();
                        state = State.CHANNEL;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case UNIVERSE_LOCATION: {
                    if (c == FeaturePackLocation.UNIVERSE_LOCATION_END) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.universeLocation = builder.toString();
                        builder = new StringBuilder();
                        state = State.CHANNEL_SEPARATOR;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case CHANNEL_SEPARATOR: {
                    if (c != FeaturePackLocation.CHANNEL_START) {
                        throw new RuntimeException("Invalid syntax, no channel separator");
                    } else {
                        parsedLocation.marker = offset - 1;
                        state = State.CHANNEL;
                    }
                    break;
                }
                case CHANNEL: {
                    if (c == FeaturePackLocation.FREQUENCY_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.channel = builder.toString();
                        builder = new StringBuilder();
                        state = State.FREQUENCY;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case FREQUENCY: {
                    if (c == FeaturePackLocation.BUILD_START) {
                        parsedLocation.marker = offset - 1;
                        parsedLocation.frequency = builder.toString();
                        builder = new StringBuilder();
                        state = State.BUILD;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
            }
        }
        // Need to close the content, this is what needs to be completed
        switch (state) {
            case PRODUCER: {
                parsedLocation.producer = builder.toString();
                consumer.completeProducer(parsedLocation.producer);
                break;
            }
            case UNIVERSE: {
                // Can be a name or a factory...
                parsedLocation.universeFactory = builder.toString();
                consumer.completeUniverse(parsedLocation, parsedLocation.universeFactory);
                break;
            }
            case UNIVERSE_LOCATION: {
                parsedLocation.universeLocation = builder.toString();
                consumer.completeUniverseLocation(parsedLocation, parsedLocation.universeLocation);
                break;
            }
            case CHANNEL_SEPARATOR: {
                consumer.completeChannelSeparator(parsedLocation);
                break;
            }
            case CHANNEL: {
                parsedLocation.channel = builder.toString();
                consumer.completeChannel(parsedLocation, parsedLocation.channel);
                break;
            }
            case FREQUENCY: {
                parsedLocation.frequency = builder.toString();
                consumer.completeFrequency(parsedLocation, parsedLocation.frequency);
                break;
            }
            case BUILD: {
                parsedLocation.build = builder.toString();
                consumer.completeBuild(parsedLocation, parsedLocation.build);
                break;
            }
        }
        return parsedLocation;
    }
}
