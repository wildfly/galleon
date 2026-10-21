/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.diff.ProvisioningDiffProvider;

/**
 *
 * @author Alexey Loubyansky
 */
public interface StateDiffPlugin extends ProvisioningPlugin {

    void diff(ProvisioningDiffProvider diffProvider) throws ProvisioningException;
}
