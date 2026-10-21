/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ConfigItemContainerBuilder<B extends ConfigItemContainerBuilder<B>> {

    B addConfigItem(ConfigItem item);

    default B addFeatureGroup(FeatureGroup dep) {
        return addConfigItem(dep);
    }

    default B addFeature(FeatureConfig feature) {
        return addConfigItem(feature);
    }
}
