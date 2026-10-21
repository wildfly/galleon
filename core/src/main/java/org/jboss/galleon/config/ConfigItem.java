/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config;

/**
 * @author Alexey Loubyansky
 *
 */
public interface ConfigItem {

    String getOrigin();

    boolean isGroup();
}
