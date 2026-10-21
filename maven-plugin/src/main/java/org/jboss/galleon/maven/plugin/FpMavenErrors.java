/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.maven.plugin;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FpMavenErrors {

    static String propertyMissing(String prop) {
        return "Property " + prop + " is missing";
    }

    static String featurePackBuild() {
        return "Failed to build feature-pack";
    }

    static String featurePackInstallation() {
        return "Failed to install feature-pack into repository";
    }

    static String artifactResolution(String gav) {
        return "Failed to resolve " + gav;
    }

    static String artifactResolution() {
        return "Failed to resolve one of the artifacts";
    }

    static String artifactMissing(String gav) {
        return "Repository is missing artifact " + gav;
    }
}
