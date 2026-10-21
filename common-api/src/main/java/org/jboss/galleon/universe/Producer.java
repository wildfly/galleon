/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import java.util.Collection;

import org.jboss.galleon.ProvisioningException;

/**
 * Feature-pack producer
 *
 * @author Alexey Loubyansky
 */
public interface Producer<C extends Channel> {

    String getName();

    boolean hasFrequencies();

    Collection<String> getFrequencies();

    boolean hasDefaultFrequency();

    String getDefaultFrequency();

    boolean hasChannel(String name) throws ProvisioningException;

    C getChannel(String name) throws ProvisioningException;

    Collection<C> getChannels() throws ProvisioningException;

    boolean hasDefaultChannel() throws ProvisioningException;

    C getDefaultChannel() throws ProvisioningException;
}
