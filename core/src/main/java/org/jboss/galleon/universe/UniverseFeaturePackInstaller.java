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
package org.jboss.galleon.universe;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public interface UniverseFeaturePackInstaller {

    static Map<String, UniverseFeaturePackInstaller> load() throws ProvisioningException {
        final ServiceLoader<UniverseFeaturePackInstaller> loader = ServiceLoader.load(UniverseFeaturePackInstaller.class);
        Map<String, UniverseFeaturePackInstaller> universeInstallers = Collections.emptyMap();
        for(UniverseFeaturePackInstaller uCreator : loader) {
            if(universeInstallers.isEmpty()) {
                universeInstallers = Collections.singletonMap(uCreator.getUniverseFactoryId(), uCreator);
                continue;
            }
            if(universeInstallers.containsKey(uCreator.getUniverseFactoryId())) {
                throw new IllegalStateException("Only one universe feature-pack installer is allowed per repository type "
                        + uCreator.getUniverseFactoryId() + " but found " + uCreator + " and " + universeInstallers.get(uCreator.getUniverseFactoryId()));
            }
            if(universeInstallers.size() == 1) {
                final HashMap<String, UniverseFeaturePackInstaller> tmp = new HashMap<>(2);
                tmp.putAll(universeInstallers);
                universeInstallers = tmp;
            }
            universeInstallers.put(uCreator.getUniverseFactoryId(), uCreator);
        }
        return CollectionUtils.unmodifiable(universeInstallers);
    }

    /**
     * Universe factory id
     *
     * @return  universe factory id
     */
    String getUniverseFactoryId();

    /**
     * Installs feature-pack archive into the target repository.
     *
     * @param universe  target universe
     * @param fpid  feature-pack id
     * @param fpZip  feature-pack archive
     * @throws ProvisioningException  in case anything goes wrong
     */
    void install(Universe<?> universe, FeaturePackLocation.FPID fpid, Path fpZip) throws ProvisioningException;
}
