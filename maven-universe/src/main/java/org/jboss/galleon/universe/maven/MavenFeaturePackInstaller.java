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

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenFeaturePackInstaller implements UniverseFeaturePackInstaller {

    public static final String ZIP = "zip";

    @Override
    public String getUniverseFactoryId() {
        return MavenUniverseFactory.ID;
    }

    @Override
    public void install(Universe<?> universe, FPID fpid, Path fpZip) throws ProvisioningException {
        if(MvnNoLocUniverse.class.isAssignableFrom(universe.getClass())) {
            ((MvnNoLocUniverse) universe).install(fpid, fpZip);
            return;
        }
        final MavenUniverse mvnUni = (MavenUniverse) universe;
        final FeaturePackLocation fps = fpid.getLocation();
        final MavenProducer producer = mvnUni.getProducer(fps.getProducerName());
        // make sure the channel exists
        producer.getChannel(fps.getChannelName());

        final MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(producer.getFeaturePackGroupId());
        artifact.setArtifactId(producer.getFeaturePackArtifactId());
        artifact.setVersion(fpid.getBuild());
        artifact.setExtension(ZIP);

        producer.getRepo().install(artifact, fpZip);
    }
}
