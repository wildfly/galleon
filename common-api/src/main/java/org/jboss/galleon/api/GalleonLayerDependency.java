/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

/**
 *
 * @author jdenise
 */
public interface GalleonLayerDependency {

    public String getName();
    public boolean isOptional();

}
