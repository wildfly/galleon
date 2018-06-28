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
