/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

    default FPID getMavenCoordinatesLocation(FPID original) {
        return original;
    }
}
