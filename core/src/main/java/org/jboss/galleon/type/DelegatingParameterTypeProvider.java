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
public abstract class DelegatingParameterTypeProvider implements ParameterTypeProvider {

    private final ParameterTypeProvider delegate;

    protected DelegatingParameterTypeProvider(ParameterTypeProvider delegate) {
        assert delegate != null : "delegate is null";
        this.delegate = delegate;
    }

    @Override
    public FeatureParameterType getType(ProducerSpec producer, String name) throws ParameterTypeNotFoundException {
        final FeatureParameterType type = resolveType(producer, name);
        return type == null ? delegate.getType(producer, name) : type;
    }

    protected abstract FeatureParameterType resolveType(ProducerSpec fpChannel, String name);
}
