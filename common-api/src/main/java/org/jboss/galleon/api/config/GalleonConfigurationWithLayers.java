/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.config;

import java.util.Map;
import java.util.Set;
import org.jboss.galleon.config.ConfigId;

/**
 * @author Alexey Loubyansky
 *
 */
public interface GalleonConfigurationWithLayers {

    public ConfigId getId();

    public String getModel();

    public String getName();

    public boolean hasProperties();

    public Map<String, String> getProperties();

    public boolean hasConfigDeps();

    public Map<String, ConfigId> getConfigDeps();

    public boolean isInheritLayers();

    public boolean hasIncludedLayers();

    public Set<String> getIncludedLayers();

    public boolean isLayerIncluded(String layerName);

    public boolean hasExcludedLayers();

    public Set<String> getExcludedLayers();

    public boolean isLayerExcluded(String layerName);

    public static ConfigId toId(GalleonConfigurationWithLayers config) {
        return new ConfigId(config.getModel(), config.getName());
    }

}
