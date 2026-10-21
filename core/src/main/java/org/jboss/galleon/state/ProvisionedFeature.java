/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.state;

import java.util.Collection;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProvisionedFeature {

    boolean hasId();

    ResolvedFeatureId getId();

    ResolvedSpecId getSpecId();

    boolean hasParams();

    Collection<String> getParamNames();

    String getConfigParam(String name) throws ProvisioningException;

    Object getResolvedParam(String name) throws ProvisioningException;

    Map<String, Object> getResolvedParams();
}
