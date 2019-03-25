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
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class MvnNoLocUniverse implements Universe<MvnNoLocUniverse>, Producer<MvnNoLocUniverse>, Channel {

    public static final String NAME = "no-loc";

    protected final MavenRepoManager repo;

    public MvnNoLocUniverse(MavenRepoManager repo) {
        this.repo = repo;
    }

    @Override
    public String getFactoryId() {
        return MavenUniverseConstants.MAVEN;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public boolean hasProducer(String producerName) throws ProvisioningException {
        return true;
    }

    @Override
    public MvnNoLocUniverse getProducer(String producerName) throws ProvisioningException {
        return this;
    }

    @Override
    public Collection<MvnNoLocUniverse> getProducers() throws ProvisioningException {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean hasFrequencies() {
        return false;
    }

    @Override
    public Collection<String> getFrequencies() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasDefaultFrequency() {
        return false;
    }

    @Override
    public String getDefaultFrequency() {
        return null;
    }

    @Override
    public boolean hasChannel(String name) throws ProvisioningException {
        return NAME.equals(name);
    }

    @Override
    public MvnNoLocUniverse getChannel(String name) throws ProvisioningException {
        return name == null || NAME.equals(name) ? this : null;
    }

    @Override
    public Collection<MvnNoLocUniverse> getChannels() throws ProvisioningException {
        return Collections.emptyList();
    }

    @Override
    public boolean hasDefaultChannel() {
        return false;
    }

    @Override
    public MvnNoLocUniverse getDefaultChannel() {
        return null;
    }

    @Override
    public String getLatestBuild(FeaturePackLocation fpl) throws ProvisioningException {
        return getLatestBuild(fpl.getFPID());
    }

    @Override
    public List<String> getAllBuilds(FeaturePackLocation fpl) throws ProvisioningException {
        return repo.getAllVersions(toArtifact(fpl.getFPID()));
    }

    @Override
    public String getLatestBuild(FPID fpid) throws ProvisioningException {
        return repo.getLatestVersion(toArtifact(fpid));
    }

    @Override
    public Path resolve(FeaturePackLocation fpl) throws ProvisioningException {
        final MavenArtifact artifact = toArtifact(fpl.getFPID());
        repo.resolve(artifact);
        return artifact.getPath();
    }

    @Override
    public boolean isResolved(FeaturePackLocation fpl) throws ProvisioningException {
        repo.isResolved(toArtifact(fpl.getFPID()));
        return false;
    }

    @Override
    public boolean isDevBuild(FPID fpid) {
        return new MavenArtifactVersion(fpid.getBuild()).isSnapshot();
    }

    public void install(FPID fpid, Path fpZip) throws ProvisioningException {
        repo.install(toArtifact(fpid), fpZip);
    }

    private static MavenArtifact toArtifact(FPID fpid) throws ProvisioningException {
        final String coords = fpid.getProducer().getName();
        final MavenArtifact artifact = new MavenArtifact();
        int colon = nextColon(coords, 0);
        artifact.setGroupId(coords.substring(0, colon));
        int prevColon = colon;
        colon = nextColon(coords, colon);
        artifact.setArtifactId(coords.substring(prevColon + 1, colon));
        prevColon = colon;
        colon = nextColon(coords, colon);
        artifact.setClassifier(coords.substring(prevColon + 1, colon));
        artifact.setExtension(coords.substring(colon + 1));
        artifact.setVersion(fpid.getBuild());
        return artifact;
    }

    private static int nextColon(final String coords, int colon) throws ProvisioningException {
        colon = coords.indexOf(':', colon + 1);
        if(colon < 0) {
            throw new ProvisioningException(invalidCoords(coords));
        }
        return colon;
    }

    private static String invalidCoords(String coords) {
        return coords + " does not follow format groupId:artifactId:[classifier]:extension:version";
    }
}
