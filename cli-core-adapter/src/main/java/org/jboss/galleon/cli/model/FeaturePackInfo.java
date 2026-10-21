/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeaturePackInfo extends FeatureContainer {

    private final String description;

    FeaturePackInfo(String name, FPID fpid, ProvisioningConfig config) {
        super(name, fpid, config);
        this.description = "No Description available yet";
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
