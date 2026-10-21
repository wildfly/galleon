/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

/**
 *
 * @author jdenise
 */
public class GalleonFeatureParamSpec {

    private final String name;
    private final String stability;

    public GalleonFeatureParamSpec(String name, String stability) {
        this.name = name;
        this.stability = stability;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the stability
     */
    public String getStability() {
        return stability;
    }

}
