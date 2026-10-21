/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.plugin.ProvisioningPlugin;

/**
 *
 * @author Alexey Loubyansky
 * @author jdenise@redhat.com
 */
public interface FeaturePackPluginVisitor<T extends ProvisioningPlugin> {
    void visitPlugin(T plugin) throws ProvisioningException;
}