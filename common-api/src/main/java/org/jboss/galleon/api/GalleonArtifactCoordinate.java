/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

/**
 *
 * @author jdenise
 */
public interface GalleonArtifactCoordinate {

    String getGroupId();

    String getArtifactId();

    String getVersion();

    String getExtension();

    String getClassifier();
}
