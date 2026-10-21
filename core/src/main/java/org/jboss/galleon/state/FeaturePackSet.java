/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.List;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FeaturePackSet<F extends FeaturePack<?>> {

    boolean hasFeaturePacks();

    Collection<F> getFeaturePacks();

    boolean hasFeaturePack(ProducerSpec producer);

    F getFeaturePack(ProducerSpec producer) throws ProvisioningException;

    boolean hasConfigs();

    List<ProvisionedConfig> getConfigs();
}
