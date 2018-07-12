/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

package org.jboss.galleon.creator;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Universe;

/**
 *
 * @author Alexey Loubyansky
 */
public interface UniverseFeaturePackCreator {

    /**
     * Universe factory id
     *
     * @return  universe factory id
     */
    String getUniverseFactoryId();

    /**
     * Creates a package from the directory containing the feature-pack content
     * and installs it into the target repository.
     *
     * @param universe  target universe
     * @param fpid  feature-pack id
     * @param fpContentDir  directory containing prepared feature-pack content
     * @throws ProvisioningException  in case anything goes wrong
     */
    void install(Universe<?> universe, FeaturePackLocation.FPID fpid, Path fpContentDir) throws ProvisioningException;
}
