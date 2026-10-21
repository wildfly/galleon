/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jdenise
 */
public interface GalleonLayer {

    public String getName();

    public String getModel();

    /**
     * @return the dependencies
     */
    public Collection<GalleonLayerDependency> getLayerDeps();

    /**
     * @return the properties
     */
    public Map<String, String> getProperties();

}
