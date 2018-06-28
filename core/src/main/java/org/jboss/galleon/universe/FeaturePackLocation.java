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

package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningDescriptionException;

/**
 * Complete feature-pack location incorporates two things: the feature-pack
 * identity and its origin.
 *
 * The identity is used to check whether the feature-pack is present in
 * the installation, check version compatibility, etc.
 *
 * The origin is used to obtain the feature-pack and later after it has been
 * installed to check for version updates.
 *
 * The string format for the complete location is producer[@factory[(location)]]:channel[/frequency]#build
 *
 * 'factory[(location)]' above is a universe specification. Factory is an ID of the universe factory and
 * an optional location is used by the factory to create the universe.
 *
 * Factory and location can be aliased in the configs, in that case the location becomes
 * producer[@universe]:channel[/frequency]#build
 *
 * Producer may represent a product or a project.
 *
 * Universe is a set of producers.
 *
 * Channel represents a stream of backward compatible version updates.
 *
 * Frequency is an optional classifier for feature-pack builds that are
 * streamed through the channel, e.g. DR, Alpha, Beta, CR, Final, etc. It is
 * basically the channel's feature-pack build filter.
 *
 * Build is an ID or version of the feature-pack which must be unique in the scope of the channel.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLocation {

    public static final char BUILD_START = '#';
    public static final char CHANNEL_START = ':';
    public static final char FREQUENCY_START = '/';
    public static final char UNIVERSE_LOCATION_END = ')';
    public static final char UNIVERSE_LOCATION_START = '(';
    public static final char UNIVERSE_START = '@';

    public class FPID {

        private final int hash;

        private FPID() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + getChannel().hashCode();
            hash = prime * hash + (build == null ? 0 : build.hashCode());
            this.hash = hash;
        }

        public ProducerSpec getProducer() {
            return FeaturePackLocation.this.getProducer();
        }

        public ChannelSpec getChannel() {
            return FeaturePackLocation.this.getChannel();
        }

        public String getBuild() {
            return build;
        }

        public FeaturePackLocation getLocation() {
            return FeaturePackLocation.this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FPID other = (FPID) obj;
            Object thisField = getChannel();
            Object otherField = other.getChannel();
            if (thisField == null) {
                if (otherField != null)
                    return false;
            } else if (!thisField.equals(otherField))
                return false;
            thisField = getBuild();
            otherField = other.getBuild();
            if (thisField == null) {
                if (otherField != null)
                    return false;
            } else if (!thisField.equals(otherField))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return FeaturePackLocation.toString(universeSpec, producer, channel, null, build);
        }
    }

    public class ChannelSpec {

        private final int hash;

        private ChannelSpec() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + (channel == null ? 0 : channel.hashCode());
            hash = prime * hash + producer.hashCode();
            hash = prime * hash + (universeSpec == null ? 0 : universeSpec.hashCode());
            this.hash = hash;
        }

        public UniverseSpec getUniverse() {
            return universeSpec;
        }

        public String getProducer() {
            return producer;
        }

        public String getName() {
            return channel;
        }

        public FeaturePackLocation getLocation() {
            return FeaturePackLocation.this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ChannelSpec other = (ChannelSpec) obj;
            Object otherField = other.getName();
            if (channel == null) {
                if (otherField != null)
                    return false;
            } else if (!channel.equals(otherField))
                return false;
            otherField = other.getProducer();
            if (producer == null) {
                if (otherField != null)
                    return false;
            } else if (!producer.equals(otherField))
                return false;
            otherField = other.getUniverse();
            if (universeSpec == null) {
                if (otherField != null)
                    return false;
            } else if (!universeSpec.equals(otherField))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return FeaturePackLocation.toString(universeSpec, producer, channel, null, null);
        }
    }

    public class ProducerSpec {

        private final int hash;

        private ProducerSpec() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + producer.hashCode();
            hash = prime * hash + (universeSpec == null ? 0 : universeSpec.hashCode());
            this.hash = hash;
        }

        public UniverseSpec getUniverse() {
            return universeSpec;
        }

        public String getName() {
            return producer;
        }

        public FeaturePackLocation getLocation() {
            return FeaturePackLocation.this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProducerSpec other = (ProducerSpec) obj;
            Object otherField = other.getName();
            if (producer == null) {
                if (otherField != null)
                    return false;
            } else if (!producer.equals(otherField))
                return false;
            otherField = other.getUniverse();
            if (universeSpec == null) {
                if (otherField != null)
                    return false;
            } else if (!universeSpec.equals(otherField))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return FeaturePackLocation.toString(universeSpec, producer, null, null, null);
        }
    }

    /**
     * Creates feature-pack location from its string representation.
     *
     * @param str  string representation of a feature-pack location
     * @return  feature-pack source
     * @throws ProvisioningDescriptionException  in case the string is not following the syntax
     */
    public static FeaturePackLocation fromString(String str) {
        if(str == null) {
            throw new IllegalArgumentException("str is null");
        }

        int buildSep = str.lastIndexOf(BUILD_START);
        if(buildSep < 0) {
            buildSep = str.length();
        }
        int universeEnd = buildSep;
        int channelNameEnd = buildSep;
        int producerEnd = 0;
        loop: while(universeEnd > 0) {
            switch(str.charAt(--universeEnd)) {
                case FREQUENCY_START:
                    channelNameEnd = universeEnd;
                    break;
                case UNIVERSE_START:
                    producerEnd = universeEnd;
                    universeEnd = buildSep;
                case CHANNEL_START:
                    break loop;
            }
        }
        if(universeEnd <= 0) {
            throw unexpectedFormat(str);
        }
        if (producerEnd == 0) {
            while (producerEnd < universeEnd) {
                if (str.charAt(producerEnd) == UNIVERSE_START) {
                    break;
                }
                ++producerEnd;
            }
            if(producerEnd == 0) {
                throw unexpectedFormat(str);
            }
        }
        return new FeaturePackLocation(
                producerEnd == universeEnd ? null : UniverseSpec.fromString(str.substring(producerEnd + 1, universeEnd)),
                str.substring(0, producerEnd),
                universeEnd == channelNameEnd ? null : str.substring(universeEnd + 1, channelNameEnd),
                channelNameEnd == buildSep ? null : str.substring(channelNameEnd + 1, buildSep),
                buildSep == str.length() ? null : str.substring(buildSep + 1)
                );
    }

    private static IllegalArgumentException unexpectedFormat(String str) {
        return new IllegalArgumentException(str + " does not follow format producer[@factory[(location)]]:channel[/frequency]#build");
    }

    private static String toString(UniverseSpec universeSpec, String producer, String channel, String frequency, String build) {
        final StringBuilder buf = new StringBuilder();
        buf.append(producer);
        if(universeSpec != null) {
            buf.append(UNIVERSE_START).append(universeSpec);
        }
        if(channel != null) {
            buf.append(CHANNEL_START).append(channel);
        }
        if(frequency != null) {
            buf.append(FREQUENCY_START).append(frequency);
        }
        if(build != null) {
            buf.append(BUILD_START).append(build);
        }
        return buf.toString();
    }

    private final UniverseSpec universeSpec;
    private final String producer;
    private final String channel;
    private final String frequency;
    private final String build;

    private ProducerSpec producerSpec;
    private ChannelSpec channelSpec;
    private FPID fpid;

    private final int hash;

    public FeaturePackLocation(UniverseSpec universeSpec, String producer, String channelName, String frequency,
            String build) {
        this.universeSpec = universeSpec;
        this.producer = producer;
        this.channel = channelName;
        this.frequency = frequency;
        this.build = build;

        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((build == null) ? 0 : build.hashCode());
        hash = prime * hash + ((channel == null) ? 0 : channel.hashCode());
        hash = prime * hash + ((frequency == null) ? 0 : frequency.hashCode());
        hash = prime * hash + producer.hashCode();
        hash = prime * hash + (universeSpec == null ? 0 : universeSpec.hashCode());
        this.hash = hash;
    }

    public boolean hasUniverse() {
        return universeSpec != null;
    }

    public UniverseSpec getUniverse() {
        return universeSpec;
    }

    public String getProducerName() {
        return producer;
    }

    public String getChannelName() {
        return channel;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getBuild() {
        return build;
    }

    public ProducerSpec getProducer() {
        return producerSpec == null ? producerSpec = new ProducerSpec() : producerSpec;
    }

    public ChannelSpec getChannel() {
        return channelSpec == null ? channelSpec = new ChannelSpec() : channelSpec;
    }

    public FPID getFPID() {
        return fpid == null ? fpid = new FPID() : fpid;
    }

    public FeaturePackLocation replaceUniverse(UniverseSpec universe) {
        return new FeaturePackLocation(universe, producer, channel, frequency, build);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeaturePackLocation other = (FeaturePackLocation) obj;
        if (build == null) {
            if (other.build != null)
                return false;
        } else if (!build.equals(other.build))
            return false;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        if (frequency == null) {
            if (other.frequency != null)
                return false;
        } else if (!frequency.equals(other.frequency))
            return false;
        if (producer == null) {
            if (other.producer != null)
                return false;
        } else if (!producer.equals(other.producer))
            return false;
        if (universeSpec == null) {
            if (other.universeSpec != null)
                return false;
        } else if (!universeSpec.equals(other.universeSpec))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return toString(universeSpec, producer, channel, frequency, build);
    }
}
