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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackCreator extends UniverseResolverBuilder<FeaturePackCreator> {

    public static FeaturePackCreator getInstance() {
        return new FeaturePackCreator();
    }

    private static Map<String, UniverseFeaturePackCreator> loadUniverseFpCreators() throws ProvisioningException {
        final ServiceLoader<UniverseFeaturePackCreator> loader = ServiceLoader.load(UniverseFeaturePackCreator.class);
        Map<String, UniverseFeaturePackCreator> universeCreators = Collections.emptyMap();
        for(UniverseFeaturePackCreator uCreator : loader) {
            if(universeCreators.isEmpty()) {
                universeCreators = Collections.singletonMap(uCreator.getUniverseFactoryId(), uCreator);
                continue;
            }
            if(universeCreators.containsKey(uCreator.getUniverseFactoryId())) {
                throw new IllegalStateException("Only one universe feature-pack creator is allowed per repository type "
                        + uCreator.getUniverseFactoryId() + " but found " + uCreator + " and " + universeCreators.get(uCreator.getUniverseFactoryId()));
            }
            if(universeCreators.size() == 1) {
                final HashMap<String, UniverseFeaturePackCreator> tmp = new HashMap<>(2);
                tmp.putAll(universeCreators);
                universeCreators = tmp;
            }
            universeCreators.put(uCreator.getUniverseFactoryId(), uCreator);
        }
        return CollectionUtils.unmodifiable(universeCreators);
    }

    private Map<String, UniverseFeaturePackCreator> ufpCreators;
    private List<FeaturePackBuilder> fps = Collections.emptyList();
    private Path workDir;
    private UniverseResolver universeResolver;

    public FeaturePackBuilder newFeaturePack() {
        final FeaturePackBuilder fp = new FeaturePackBuilder(this);
        addFeaturePack(fp);
        return fp;
    }

    public FeaturePackBuilder newFeaturePack(FeaturePackLocation.FPID fpid) {
        final FeaturePackBuilder fp = new FeaturePackBuilder(this);
        if (fpid != null) {
            fp.setFPID(fpid);
        }
        addFeaturePack(fp);
        return fp;
    }

    public FeaturePackCreator addFeaturePack(FeaturePackBuilder fp) {
        fps = CollectionUtils.add(fps, fp);
        return this;
    }

    public void install() throws ProvisioningException {
        ufpCreators = loadUniverseFpCreators();
        universeResolver = buildUniverseResolver();
        try {
            for (FeaturePackBuilder fp : fps) {
                fp.build();
            }
        } finally {
            if (workDir != null) {
                IoUtils.recursiveDelete(workDir);
            }
        }
    }

    void install(FeaturePackLocation.FPID fpid, Path fpContentDir) throws ProvisioningException {
        final Universe<?> universe = universeResolver.getUniverse(fpid.getLocation().getUniverse());
        final UniverseFeaturePackCreator ufpCreator = ufpCreators.get(universe.getFactoryId());
        if(ufpCreator == null) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Failed to locate an implementation of ")
            .append(UniverseFeaturePackCreator.class.getName())
            .append(" for universe repository ")
            .append(universe.getFactoryId())
            .append(" on the classpath. Available universe repositories include ");
            StringUtils.append(buf, ufpCreators.keySet());
            throw new ProvisioningException(buf.toString());
        }
        ufpCreator.install(universe, fpid, fpContentDir);
    }

    Path getWorkDir() throws ProvisioningException {
        if(workDir == null) {
            try {
                workDir = Files.createTempDirectory("gln-fp-installer");
            } catch (IOException e) {
                throw new ProvisioningException("Failed to create a tmp dir");
            }
        }
        return workDir;
    }
}
