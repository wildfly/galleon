/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.repo;

/**
 *
 * @author Alexey Loubyansky
 */
public interface MavenArtifactVersionRange {

    boolean includesVersion(MavenArtifactVersion version);
}
