/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.galleon1;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repo.RepositoryArtifactInstaller;
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

        if (!(mvnUni.artifactResolver instanceof RepositoryArtifactInstaller)) {
            throw new ProvisioningException(mvnUni.artifactResolver.getClass().getName() + " is not an instance of " + LegacyGalleon1RepositoryManager.class.getName());
        }

        ((RepositoryArtifactInstaller) mvnUni.artifactResolver).install(LegacyGalleon1Universe.toMavenCoords(fpid.getLocation()), fpZip);
    }
}
