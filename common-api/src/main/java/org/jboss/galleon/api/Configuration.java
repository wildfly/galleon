/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
public class Configuration {

    private String model;
    private String name;
    private List<String> layers = Collections.emptyList();
    private List<String> excludedLayers = Collections.emptyList();
    private Map<String, String> props = Collections.emptyMap();

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public List<String> getLayers() {
        return layers;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public List<String> getExcludedLayers() {
        return excludedLayers;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public void setExcludedLayers(List<String> excludedLayers) {
        this.excludedLayers = excludedLayers;
    }

    public static ConfigId toId(Configuration config) {
        return new ConfigId(config.getModel(), config.getName());
    }
}
