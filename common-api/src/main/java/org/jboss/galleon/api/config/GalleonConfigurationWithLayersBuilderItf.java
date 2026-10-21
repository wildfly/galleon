/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
