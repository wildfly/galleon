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

package org.jboss.galleon.universe.galleon1;

import java.nio.file.Path;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ChannelSpec;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1FeaturePackInstaller implements UniverseFeaturePackInstaller {

    @Override
    public String getUniverseFactoryId() {
        return LegacyGalleon1UniverseFactory.ID;
    }

    @Override
    public void install(Universe<?> universe, FeaturePackLocation.FPID fpid, Path fpZip) throws ProvisioningException {
        final LegacyGalleon1Universe mvnUni = (LegacyGalleon1Universe) universe;
        final ChannelSpec channel = fpid.getChannel();
        final LegacyGalleon1Producer producer = mvnUni.getProducer(channel.getProducer());
        // make sure the channel exists
        producer.getChannel(channel.getName());

        if(!(mvnUni.artifactResolver instanceof ArtifactRepositoryManager)) {
            throw new ProvisioningException(mvnUni.artifactResolver.getClass().getName() + " is not an instance of " + ArtifactRepositoryManager.class.getName());
        }

        ((ArtifactRepositoryManager) mvnUni.artifactResolver).install(LegacyGalleon1Universe.toArtifactCoords(fpid.getLocation()), fpZip);
    }
}
