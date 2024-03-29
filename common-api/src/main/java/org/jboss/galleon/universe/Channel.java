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
package org.jboss.galleon.universe;

import java.nio.file.Path;
import java.util.List;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 * Feature-pack channel
 *
 * @author Alexey Loubyansky
 */
public interface Channel {

    String getName();

    String getLatestBuild(FeaturePackLocation fpl) throws ProvisioningException;

    List<String> getAllBuilds(FeaturePackLocation fpl) throws ProvisioningException;

    String getLatestBuild(FPID fpid) throws ProvisioningException;

    Path resolve(FeaturePackLocation fpl) throws ProvisioningException;

    boolean isResolved(FeaturePackLocation fpl) throws ProvisioningException;

    default FeaturePackUpdatePlan getUpdatePlan(FeaturePackUpdatePlan.Request updateRequest) throws ProvisioningException {
        final FeaturePackLocation fpl = updateRequest.getInstalledLocation();
        String latestBuild = null;
        try {
            latestBuild = getLatestBuild(fpl);
        } catch(LatestVersionNotAvailableException e) {
            // that also means no update
        }
        if (latestBuild != null && !latestBuild.isEmpty() && !(latestBuild.equals(fpl.getBuild()))) {
            updateRequest.setNewLocation(fpl.replaceBuild(latestBuild));
        }
        return updateRequest.buildPlan();
    }

    boolean isDevBuild(FPID fpid);
}
