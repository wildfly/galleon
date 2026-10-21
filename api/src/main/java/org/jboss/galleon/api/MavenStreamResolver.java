/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.galleon.api;

/**
 *
 * @author jdenise
 */
public interface MavenStreamResolver {
    public String getLatestVersion(String groupId, String artifactId, String extension, String classifier, String baseVersion);
}
