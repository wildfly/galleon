/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class ProvisioningInfo extends FeatureContainer {

    ProvisioningInfo(ProvisioningConfig config) {
        super(null, null, config);
    }
}
