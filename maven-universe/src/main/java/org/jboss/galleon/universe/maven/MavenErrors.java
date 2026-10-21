/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

    public static String msgUniverseNotFound(String location) {
        return "Failed to resolve Maven universe " + location;
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
        return "Failed to determine the latest version of " + str + ". Make sure the remote Maven repository is properly configured and available.";
    }
}
