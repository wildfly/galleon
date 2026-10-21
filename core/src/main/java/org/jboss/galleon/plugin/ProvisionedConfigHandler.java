/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.plugin;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.ResolvedFeatureSpec;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.universe.FeaturePackLocation;


/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisionedConfigHandler {

    default void prepare(ProvisionedConfig config) throws ProvisioningException {};

    default void nextFeaturePack(FeaturePackLocation.FPID fpid) throws ProvisioningException {};

    default void nextSpec(ResolvedFeatureSpec spec) throws ProvisioningException {};

    default void nextFeature(ProvisionedFeature feature) throws ProvisioningException {};

    default void startBatch() throws ProvisioningException {};

    default void endBatch() throws ProvisioningException {};

    default void startBranch() throws ProvisioningException {};

    default void endBranch() throws ProvisioningException {};

    default void done() throws ProvisioningException {};
}