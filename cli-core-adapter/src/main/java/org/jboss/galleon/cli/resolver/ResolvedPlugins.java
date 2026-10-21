/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.resolver;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.ProvisioningOption;

/**
 *
 * @author jdenise@redhat.com
 */
public class ResolvedPlugins {

    private final Set<ProvisioningOption> install;
    private final Set<ProvisioningOption> diff;

    ResolvedPlugins(Set<ProvisioningOption> install, Set<ProvisioningOption> diff) {
        this.install = Collections.unmodifiableSet(install);
        this.diff = Collections.unmodifiableSet(diff);
    }

    /**
     * @return the install
     */
    public Set<ProvisioningOption> getInstall() {
        return install;
    }

    /**
     * @return the diff
     */
    public Set<ProvisioningOption> getDiff() {
        return diff;
    }

}
