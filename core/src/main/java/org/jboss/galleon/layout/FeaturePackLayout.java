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
package org.jboss.galleon.layout;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.xml.FeatureSpecXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public interface FeaturePackLayout {

    int DIRECT_DEP = 0;
    int TRANSITIVE_DEP = 1;
    int PATCH = 2;

    FPID getFPID();

    FeaturePackSpec getSpec();

    Path getDir();

    int getType();

    default boolean isDirectDep() {
        return getType() == DIRECT_DEP;
    }

    default boolean isTransitiveDep() {
        return getType() == TRANSITIVE_DEP;
    }

    default boolean isPatch() {
        return getType() == PATCH;
    }

    /**
     * Returns a resource path for a feature-pack.
     *
     * @param path  path to the resource relative to the feature-pack resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack was not found in the layout
     */
    default Path getResource(String... path) throws ProvisioningDescriptionException {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return getDir().resolve(Constants.RESOURCES).resolve(path[0]);
        }
        Path p = getDir().resolve(Constants.RESOURCES);
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    default boolean hasFeatureSpec(String name) {
        return Files.exists(getDir().resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML));
    }

    default FeatureSpec loadFeatureSpec(String name) throws ProvisioningException {
        final Path specXml = getDir().resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML);
        if (!Files.exists(specXml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(specXml)) {
            return FeatureSpecXmlParser.getInstance().parse(reader);
        } catch (Exception e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(specXml), e);
        }
    }
}