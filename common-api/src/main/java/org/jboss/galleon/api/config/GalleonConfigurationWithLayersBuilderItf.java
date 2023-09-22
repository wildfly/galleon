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

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigId;

/**
 * @author Alexey Loubyansky
 *
 */
public interface GalleonConfigurationWithLayersBuilderItf {

    public GalleonConfigurationWithLayersBuilderItf setName(String name);

    public GalleonConfigurationWithLayersBuilderItf setModel(String model);

    public GalleonConfigurationWithLayersBuilderItf setProperty(String name, String value);

    public GalleonConfigurationWithLayersBuilderItf setConfigDep(String depName, ConfigId configId);

    public GalleonConfigurationWithLayersBuilderItf setInheritLayers(boolean inheritLayers);

    public GalleonConfigurationWithLayersBuilderItf includeLayer(String layerName) throws ProvisioningDescriptionException;

    public GalleonConfigurationWithLayersBuilderItf removeIncludedLayer(String layer);

    public GalleonConfigurationWithLayersBuilderItf removeExcludedLayer(String layer);

    public GalleonConfigurationWithLayersBuilderItf excludeLayer(String layerName) throws ProvisioningDescriptionException;

    public GalleonConfigurationWithLayers build() throws ProvisioningDescriptionException;

}
