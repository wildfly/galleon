/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven;

/**
 *
 * @author Alexey Loubyansky
 */
public interface MavenChannelDescription {

    String getName();

    String getVersionRange();

    String getVersionIncludeRegex();

    String getVersionExcludeRegex();
}