/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.Collections;
import java.util.List;

import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
abstract class CapabilityProvider {

    private List<CapabilityProviders> capProviders = Collections.emptyList();

    void addCapabilityProviders(CapabilityProviders providers) {
        capProviders = CollectionUtils.add(capProviders, providers);
    }

    void provided(ConfigFeatureBranch branch) {
        if(capProviders.isEmpty()) {
            return;
        }
        for(CapabilityProviders providers : capProviders) {
            providers.provided(branch);
        }
    }
}
