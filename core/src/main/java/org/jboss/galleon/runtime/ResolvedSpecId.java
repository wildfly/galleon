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
package org.jboss.galleon.runtime;

import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class ResolvedSpecId {
    final ProducerSpec producer;
    final String name;
    private final int hash;

    public ResolvedSpecId(ProducerSpec producer, String name) {
        this.producer = producer;
        this.name = name;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((producer == null) ? 0 : producer.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        hash = result;
    }

    public ProducerSpec getProducer() {
        return producer;
    }

    public String getName() {
        return name;
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
        ResolvedSpecId other = (ResolvedSpecId) obj;
        if (producer == null) {
            if (other.producer != null)
                return false;
        } else if (!producer.equals(other.producer))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return '{' + producer.toString() + "}" + name;
    }
}