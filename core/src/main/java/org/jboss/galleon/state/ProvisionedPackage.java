/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;


/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisionedPackage extends FeaturePackPackage {

    static ProvisionedPackage newInstance(String name) {
        return new ProvisionedPackageImpl(name);
    }
}
