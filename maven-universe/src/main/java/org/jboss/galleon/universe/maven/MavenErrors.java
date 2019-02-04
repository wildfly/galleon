/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

package org.jboss.galleon.universe.maven;

import java.nio.file.Path;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenErrors {

    public static String msgChannelNotFound(String producer, String channelName) {
        return "Channel " + channelName + " is not found in producer " + producer;
    }

    public static String msgProducerNotFound(String producerName) {
        return "Producer " + producerName + " is not found in the universe";
    }

    public static void missingGroupId() throws MavenUniverseException {
        throw new MavenUniverseException("Artifact is missing groupId");
    }

    public static void missingArtifactId() throws MavenUniverseException {
        throw new MavenUniverseException("Artifact is missing artifactId");
    }

    public static void missingVersion(MavenArtifact artifact) throws MavenUniverseException {
        throw new MavenUniverseException("Artifact " + artifact.getGroupId() + ':' + artifact.getArtifactId() + " is missing version");
    }

    public static void missingVersionRange(MavenArtifact artifact) throws MavenUniverseException {
        throw new MavenUniverseException("Artifact " + artifact.getGroupId() + ':' + artifact.getArtifactId() + " is missing version range");
    }

    public static void missingExtension(MavenArtifact artifact) throws MavenUniverseException {
        throw new MavenUniverseException("Artifact " + artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getVersion() + " is missing extension");
    }

    public static MavenUniverseException producerNotFound(String producerName) {
        return new MavenUniverseException(msgProducerNotFound(producerName));
    }

    public static MavenUniverseException channelNotFound(String producer, String channelName) {
        return new MavenUniverseException(msgChannelNotFound(producer, channelName));
    }

    public static MavenUniverseException artifactNotFound(MavenArtifact artifact, Path repoHome) {
        return new MavenUniverseException("Artifact " + artifact.getCoordsAsString() + " not found in " + repoHome);
    }

    public static String failedToResolveLatestVersion(String str) {
        return "Failed to determine the latest version of " + str;
    }
}
