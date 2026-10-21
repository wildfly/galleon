/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
