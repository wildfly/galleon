/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.util.List;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

public class GalleonFeaturePackDescription {

    private final FPID producer;
    private final List<FPID> dependencies;
    private final List<FPID> transitives;
    private final String galleonVersion;

    public GalleonFeaturePackDescription(FPID producer, List<FPID> dependencies, List<FPID> transitives, String galleonVersion) {
        this.producer = producer;
        this.dependencies = dependencies;
        this.transitives = transitives;
        this.galleonVersion = galleonVersion;
    }

    /**
     * @return the producer
     */
    public FPID getProducer() {
        return producer;
    }

    /**
     * @return the dependencies
     */
    public List<FPID> getDependencies() {
        return dependencies;
    }

    /**
     * @return the transitives
     */
    public List<FPID> getTransitives() {
        return transitives;
    }

    /**
     * @return the galleonVersion
     */
    public String getGalleonVersion() {
        return galleonVersion;
    }

}
