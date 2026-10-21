/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.config;

import java.util.Collection;
import java.util.Map;
import org.jboss.galleon.config.ConfigId;


/**
 *
 * @author Alexey Loubyansky
 */
public interface GalleonProvisionedConfig {

    String getName();

    String getModel();

    boolean hasProperties();

    String getProperty(String name);

    Map<String, String> getProperties();

    boolean hasLayers();

    Collection<ConfigId> getLayers();

    boolean hasFeatures();

    int size();

}
