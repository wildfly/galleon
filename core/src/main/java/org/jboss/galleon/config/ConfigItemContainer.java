/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config;

import java.util.List;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ConfigItemContainer {

    boolean hasItems();

    List<ConfigItem> getItems();

     boolean isResetFeaturePackOrigin();
}