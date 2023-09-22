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
