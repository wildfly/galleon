/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.galleon.universe.maven.MavenChannelDescription;

/**
 *
 * @author Alexey Loubyansky
 */
public class ChannelDescription implements MavenChannelDescription {

    /**
     * Producer name
     */
    @Parameter(required = true)
    String name;

    /**
     * Producer artifact version range
     */
    @Parameter(required = true)
    String versionRange;

    /**
     * Producer artifact version inclusion filter
     */
    @Parameter
    String versionIncludeRegex;

    /**
     * Producer artifact version exclusion filter
     */
    @Parameter
    String versionExcludeRegex;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersionRange() {
        return versionRange;
    }

    @Override
    public String getVersionIncludeRegex() {
        return versionIncludeRegex;
    }

    @Override
    public String getVersionExcludeRegex() {
        return versionExcludeRegex;
    }
}