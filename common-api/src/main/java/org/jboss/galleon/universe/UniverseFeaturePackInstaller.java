/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
