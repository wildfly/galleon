/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jdenise
 */
public class GalleonFeatureSpec {

    private final String name;
    private final String stability;
    private final List<GalleonFeatureParamSpec> params = new ArrayList<>();

    public GalleonFeatureSpec(String name, String stability) {
        this.name = name;
        this.stability = stability;
    }

    public void addParam(GalleonFeatureParamSpec param) {
        params.add(param);
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

    /**
     * @return the params
     */
    public List<GalleonFeatureParamSpec> getParams() {
        return Collections.unmodifiableList(params);
    }
}
