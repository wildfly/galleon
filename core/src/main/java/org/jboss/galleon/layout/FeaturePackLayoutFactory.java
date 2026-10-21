/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.layout;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FeaturePackLayoutFactory<F extends FeaturePackLayout> {

    F newFeaturePack(FeaturePackLocation fpl, FeaturePackSpec spec, Path dir, int type) throws ProvisioningException;
}