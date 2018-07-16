/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

package org.jboss.galleon.universe;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenProducerBase;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MvnUniverse {

    private static final String FP_SUFFIX = "-feature-pack";
    public static String[] frequencies = new String[] {"alpha", "beta", "snapshot"};

    public static MvnUniverse getInstance(String name, MavenRepoManager repoManager) {
        return new MvnUniverse(name, repoManager);
    }

    private final String name;
    private final MavenRepoManager repoManager;
    private List<MavenProducerBase> producers = Collections.emptyList();

    private MvnUniverse(String name, MavenRepoManager repoManager) {
        this.name = name;
        this.repoManager = repoManager;
    }

    public Path resolve(FPID fpid) throws ProvisioningException {
        final String producerName = fpid.getProducer().getName();
        for(MavenProducerBase p : producers) {
            if(p.getName().equals(producerName)) {
                return p.getChannel(fpid.getChannel().getName()).resolve(fpid.getLocation());
            }
        }
        throw new IllegalStateException("Failed to locate producer for " + fpid);
    }

    public MvnUniverse createProducer(String producerName) throws ProvisioningException {
        return createProducer(producerName, producerName + FP_SUFFIX);
    }

    public MvnUniverse createProducer(String producerName, String fpArtifactId) throws ProvisioningException {
        return createProducer(producerName, fpArtifactId, null);
    }

    public MvnUniverse createProducer(String producerName, String fpArtifactId, String defaultFrequency) throws ProvisioningException {
        final MavenProducerInstaller producer = new MavenProducerInstaller(producerName, repoManager,
                new MavenArtifact().setGroupId(TestConstants.GROUP_ID + '.' + name).setArtifactId(producerName)
                        .setVersion("1.0.0.Final"),
                TestConstants.GROUP_ID + '.' + name + '.' + producerName, fpArtifactId)
                        .addFrequencies(frequencies)
                        .addChannel("1", "[1.0.0-alpha,2.0.0-alpha)");
        if(defaultFrequency != null) {
            producer.addFrequency(defaultFrequency, true);
        }
        producers = CollectionUtils.add(producers, producer.install());
        return this;
    }

    public MvnUniverse createProducer(String producerName, int channelsTotal) throws ProvisioningException {
        return createProducer(producerName, producerName + FP_SUFFIX, channelsTotal);
    }

    public MvnUniverse createProducer(String producerName, String fpArtifactId, int channels) throws ProvisioningException {
        MavenProducerInstaller producer = new MavenProducerInstaller(producerName, repoManager,
                new MavenArtifact().setGroupId(TestConstants.GROUP_ID + '.' + name).setArtifactId(producerName)
                        .setVersion("1.0.0.Final"),
                TestConstants.GROUP_ID + '.' + name + '.' + producerName, fpArtifactId);
        while(channels > 0) {
            producer.addFrequencies(frequencies).addChannel(Integer.toString(channels),
                    new StringBuilder().append('[').append(channels).append(".0.0-alpha,").append(channels + 1).append(".0.0-alpha)").toString());
            --channels;
        }
        producers = CollectionUtils.add(producers, producer.install());
        return this;
    }

    public MavenArtifact install() throws ProvisioningException {
        final MavenArtifact universeArtifact = new MavenArtifact().setGroupId(TestConstants.GROUP_ID).setArtifactId(name).setVersion("1.0.0.Final");
        final MavenUniverseInstaller installer = new MavenUniverseInstaller(repoManager, universeArtifact);
        for(MavenProducerBase p : producers) {
            installer.addProducer(p.getName(), p.getArtifact().setPath(null).setVersionRange("[1.0,)"));
        }
        installer.install();
        return universeArtifact;
    }
}
