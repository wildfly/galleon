/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin;

import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningOption;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisioningPlugin {

    default Map<String, ProvisioningOption> getOptions() {
        return Collections.emptyMap();
    };
}
