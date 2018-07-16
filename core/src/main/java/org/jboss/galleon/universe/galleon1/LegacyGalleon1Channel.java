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

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1Channel implements Channel {

    private final LegacyGalleon1Universe universe;
    private final String name;

    LegacyGalleon1Channel(LegacyGalleon1Universe universe, String name) {
        this.universe = universe;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.Channel#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.universe.Channel#resolve(org.jboss.galleon.FeaturePackLocation)
     */
    @Override
    public Path resolve(FeaturePackLocation fpl) throws ProvisioningException {
        return universe.artifactResolver.resolve(LegacyGalleon1Universe.toArtifactCoords(fpl).toString());
    }

    @Override
    public String getLatestBuild(FeaturePackLocation fpl) throws ProvisioningException {
        throw new ProvisioningException("Failed to determine the latest build for " + fpl + ": operation not supported");
    }

    @Override
    public boolean isResolved(FeaturePackLocation fpl) throws ProvisioningException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLatestBuild(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
