/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.type;

import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ParameterTypeProvider {

    FeatureParameterType getType(ProducerSpec producer, String name) throws ParameterTypeNotFoundException;
}
