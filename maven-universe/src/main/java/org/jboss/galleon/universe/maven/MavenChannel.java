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
import java.util.Collection;
import java.util.List;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.LatestVersionNotAvailableException;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenChannel implements Channel, MavenChannelDescription {

    private final String name;
    private final String versionRange;
    private final MavenProducerBase producer;

    public MavenChannel(MavenProducerBase producer, String name, String versionRange) throws MavenUniverseException {
        assert name != null : "Producer name is missing";
        assert versionRange != null : "Producer version-range is missing";

        this.name = name;
        this.versionRange = versionRange;
        this.producer = producer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLatestBuild(FeaturePackLocation fpl) throws ProvisioningException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);
        artifact.setVersionRange(versionRange);
        try {
            return producer.getRepo().getLatestVersion(artifact, getFrequency(fpl));
        } catch(MavenLatestVersionNotAvailableException e) {
            if(fpl.getFrequency() == null && producer.hasDefaultFrequency()) {
                fpl = new FeaturePackLocation(fpl.getUniverse(), fpl.getProducerName(), fpl.getChannelName(), producer.getDefaultFrequency(), null);
            }
            throw new LatestVersionNotAvailableException(fpl);
        } catch(MavenUniverseException e) {
            throw e;
        }
    }

    @Override
    public List<String> getAllBuilds(FeaturePackLocation fpl) throws ProvisioningException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);
        artifact.setVersionRange(versionRange);
        try {
            return producer.getRepo().getAllVersions(artifact);
        } catch (MavenLatestVersionNotAvailableException e) {
            if (fpl.getFrequency() == null && producer.hasDefaultFrequency()) {
                fpl = new FeaturePackLocation(fpl.getUniverse(), fpl.getProducerName(), fpl.getChannelName(), producer.getDefaultFrequency(), null);
            }
            throw new LatestVersionNotAvailableException(fpl);
        } catch (MavenUniverseException e) {
            throw e;
        }
    }

    @Override
    public Path resolve(FeaturePackLocation fpl) throws MavenUniverseException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);

        if(fpl.getBuild() == null) {
            artifact.setVersionRange(versionRange);
            producer.getRepo().resolveLatestVersion(artifact, getFrequency(fpl));
        } else {
            artifact.setVersion(fpl.getBuild());
            producer.getRepo().resolve(artifact);
        }
        return artifact.getPath();
    }

    public String getFeaturePackGroupId() {
        return producer.getFeaturePackGroupId();
    }

    public String getFeaturePackArtifactId() {
        return producer.getFeaturePackArtifactId();
    }

    public Collection<String> getFrequencies() {
        return producer.getFrequencies();
    }

    @Override
    public String getVersionRange() {
        return versionRange;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getFeaturePackArtifactId().hashCode();
        result = prime * result + getFeaturePackGroupId().hashCode();
        result = prime * result + getFrequencies().hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((versionRange == null) ? 0 : versionRange.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MavenChannel other = (MavenChannel) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (versionRange == null) {
            if (other.versionRange != null)
                return false;
        } else if (!versionRange.equals(other.versionRange))
            return false;
        if (!getFeaturePackArtifactId().equals(other.getFeaturePackArtifactId()))
            return false;
        if (!getFeaturePackGroupId().equals(other.getFeaturePackGroupId()))
            return false;
        if (!getFrequencies().equals(other.getFrequencies())) {
            return false;
        }
        return true;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[channel ").append(name);
        buf.append(" groupId=").append(getFeaturePackGroupId());
        buf.append(" artifactId=").append(getFeaturePackArtifactId());
        buf.append(" version-range=").append(versionRange);
        buf.append(" frequencies=");
        StringUtils.append(buf, getFrequencies());
        return buf.toString();
    }

    private String getFrequency(FeaturePackLocation fpl) throws MavenUniverseException {
        final String frequency = fpl.getFrequency();
        if(frequency == null) {
            return producer.getDefaultFrequency();
        }
        if (!producer.getFrequencies().contains(frequency)) {
            throw new MavenUniverseException(Errors.frequencyNotSupported(((Producer<?>) producer).getFrequencies(), fpl));
        }
        return frequency;
    }

    @Override
    public boolean isResolved(FeaturePackLocation fpl) throws ProvisioningException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);

        if (fpl.getBuild() == null) {
            artifact.setVersionRange(versionRange);
            return producer.getRepo().isLatestVersionResolved(artifact, getFrequency(fpl));
        } else {
            artifact.setVersion(fpl.getBuild());
            return producer.getRepo().isResolved(artifact);
        }
    }

    @Override
    public String getLatestBuild(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);
        artifact.setVersionRange(versionRange);
        try {
            return producer.getRepo().getLatestVersion(artifact);
        } catch (MavenLatestVersionNotAvailableException e) {
            throw new LatestVersionNotAvailableException(fpid.getLocation());
        } catch (MavenUniverseException e) {
            throw e;
        }
    }

    @Override
    public boolean isDevBuild(FeaturePackLocation.FPID fpid) {
        return new MavenArtifactVersion(fpid.getBuild()).isSnapshot();
    }
}
