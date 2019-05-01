/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
