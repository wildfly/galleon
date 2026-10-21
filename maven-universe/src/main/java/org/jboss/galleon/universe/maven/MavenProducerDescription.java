/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

import java.util.Collection;

/**
 *
 * @author Alexey Loubyansky
 */
public interface MavenProducerDescription<C extends MavenChannelDescription> {

    String getName();

    String getFeaturePackGroupId();

    String getFeaturePackArtifactId();

    Collection<String> getFrequencies();

    String getDefaultFrequency();

    boolean hasDefaultChannel();

    String getDefaultChannelName();

    Collection<C> getChannels() throws MavenUniverseException;
}