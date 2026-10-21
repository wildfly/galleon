/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe;

import java.util.Collection;

import org.jboss.galleon.ProvisioningException;


/**
 * Universe is a set of feature-pack producers
 *
 * @author Alexey Loubyansky
 */
public interface Universe<P extends Producer<?>> {

    String getFactoryId();

    String getLocation();

    boolean hasProducer(String producerName) throws ProvisioningException;

    P getProducer(String producerName) throws ProvisioningException;

    Collection<P> getProducers() throws ProvisioningException;
}
