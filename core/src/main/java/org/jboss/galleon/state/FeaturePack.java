/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.Set;

import org.jboss.galleon.universe.FeaturePackLocation;


/**
 *
 * @author Alexey Loubyansky
 */
public interface FeaturePack<P extends FeaturePackPackage> {

    FeaturePackLocation.FPID getFPID();

    boolean hasPackages();

    boolean containsPackage(String name);

    Set<String> getPackageNames();

    Collection<P> getPackages();

    P getPackage(String name);

}
