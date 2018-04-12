/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.maven.plugin;

import java.util.Collection;

import org.jboss.galleon.ArtifactCoords;

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

    static String artifactResolution(Collection<ArtifactCoords> artifacts) {
        return "Failed to resolve artifacts: " + artifacts;
    }

    static String artifactResolution(ArtifactCoords gav) {
        return "Failed to resolve " + gav;
    }

    static String artifactMissing(ArtifactCoords gav) {
        return "Repository is missing artifact " + gav;
    }
}
