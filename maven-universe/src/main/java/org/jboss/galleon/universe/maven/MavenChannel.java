/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
import java.util.regex.Pattern;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.LatestVersionNotAvailableException;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.util.StringUtils;
import org.jboss.galleon.BaseErrors;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenChannel implements Channel, MavenChannelDescription {

    private final String name;
    private final String versionRange;
    private final MavenProducerBase producer;
    private final String versionIncludeRegex;
    private final String versionExcludeRegex;
    private final Pattern versionIncludePattern;
    private final Pattern versionExcludePattern;

    public MavenChannel(MavenProducerBase producer, String name, String versionRange) throws MavenUniverseException {
        this(producer, name, versionRange, null, null);
    }

    public MavenChannel(MavenProducerBase producer, String name, String versionRange, String versionIncludeRegex, String versionExcludeRegex) throws MavenUniverseException {
        assert name != null : "Producer name is missing";
        assert versionRange != null : "Producer version-range is missing";

        this.name = name;
        this.versionRange = versionRange;
        this.producer = producer;
        this.versionIncludeRegex = versionIncludeRegex;
        this.versionExcludeRegex = versionExcludeRegex;
        this.versionIncludePattern = versionIncludeRegex == null ? null : Pattern.compile(versionIncludeRegex);
        this.versionExcludePattern = versionExcludeRegex == null ? null : Pattern.compile(versionExcludeRegex);
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
            return producer.getRepo().getLatestVersion(artifact, getFrequency(fpl), versionIncludePattern, versionExcludePattern);
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
            return producer.getRepo().getAllVersions(artifact, versionIncludePattern, versionExcludePattern);
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
    public Path resolve(FeaturePackLocation fpl) throws ProvisioningException {
        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setExtension(MavenArtifact.EXT_ZIP);

        if(fpl.getBuild() == null) {
            artifact.setVersionRange(versionRange);
            try {
                producer.getRepo().resolveLatestVersion(artifact, getFrequency(fpl), versionIncludePattern, versionExcludePattern);
            } catch (MavenLatestVersionNotAvailableException e) {
                if (fpl.getFrequency() == null && producer.hasDefaultFrequency()) {
                    fpl = new FeaturePackLocation(fpl.getUniverse(), fpl.getProducerName(), fpl.getChannelName(), producer.getDefaultFrequency(), null);
                }
                throw new MavenLatestVersionNotAvailableException(e.getLocalizedMessage(), fpl);
            }
        } else {
            String build = fpl.getBuild();

            // Make sure this build conforms to the specification of this Channel.
            // Iterating a list isn't great but MavenRepoManager provides a list and other callers of
            // getAllVersions request a list so converting to a Set would require care
            // Iterate from the end of the list under the assumption that older versions less likely to be requested
            boolean valid = false;
            List<String> allBuilds = getAllBuilds(fpl);
            for (int i = allBuilds.size() - 1; !valid && i >= 0; i--) {
                valid = build.equals(allBuilds.get(i));
            }
            if (!valid) {
                throw new MavenUniverseException(String.format("%s is not a valid build for channel %s", build, name));
            }

            artifact.setVersion(build);
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
    public String getVersionIncludeRegex() {
        return versionIncludeRegex;
    }

    @Override
    public String getVersionExcludeRegex() {
        return versionExcludeRegex;
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
        result = prime * result + ((versionIncludeRegex == null) ? 0 : versionIncludeRegex.hashCode());
        result = prime * result + ((versionExcludeRegex == null) ? 0 : versionExcludeRegex.hashCode());
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
        if (versionIncludeRegex == null) {
            if (other.versionIncludeRegex != null)
                return false;
        } else if (!versionIncludeRegex.equals(other.versionIncludeRegex))
            return false;
        if (versionExcludeRegex == null) {
            if (other.versionExcludeRegex != null)
                return false;
        } else if (!versionExcludeRegex.equals(other.versionExcludeRegex))
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
        buf.append(" versionIncludeRegex=").append(versionIncludeRegex);
        buf.append(" versionExcludeRegex").append(versionExcludeRegex);
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
            throw new MavenUniverseException(BaseErrors.frequencyNotSupported(((Producer<?>) producer).getFrequencies(), fpl));
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
            return producer.getRepo().getLatestVersion(artifact, null, versionIncludePattern, versionExcludePattern);
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
