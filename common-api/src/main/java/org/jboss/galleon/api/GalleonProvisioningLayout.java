/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

public interface GalleonProvisioningLayout extends AutoCloseable {

    List<GalleonFeaturePackLayout> getOrderedFeaturePacks();

    List<GalleonFeaturePackLayout> getPatches(FeaturePackLocation.FPID fpid);

    GalleonFeaturePackLayout getFeaturePack(ProducerSpec spec) throws ProvisioningException;
    @Override
    void close();

    public boolean hasPlugins();

    public void uninstall(FeaturePackLocation.FPID fpid) throws ProvisioningException;
}
