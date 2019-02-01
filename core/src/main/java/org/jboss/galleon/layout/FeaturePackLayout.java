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
package org.jboss.galleon.layout;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.xml.FeaturePackXmlParser;
import org.jboss.galleon.xml.ConfigLayerSpecXmlParser;
import org.jboss.galleon.xml.FeatureSpecXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class FeaturePackLayout {

    public static final int DIRECT_DEP = 0;
    public static final int TRANSITIVE_DEP = 1;
    public static final int PATCH = 2;

    protected final FPID fpid;
    protected final int type;
    protected Path dir;
    protected FeaturePackSpec spec;

    protected FeaturePackLayout(FPID fpid, Path dir, int type) {
        this.fpid = fpid;
        this.dir = dir;
        this.type = type;
    }

    public FPID getFPID() {
        return fpid;
    }

    public FeaturePackSpec getSpec() throws ProvisioningException {
        if(spec == null) {
            try(BufferedReader reader = Files.newBufferedReader(dir.resolve(Constants.FEATURE_PACK_XML))) {
                spec = FeaturePackXmlParser.getInstance().parse(reader);
            } catch (Exception e) {
                throw new ProvisioningException(Errors.readFile(dir.resolve(Constants.FEATURE_PACK_XML)));
            }
        }
        return spec;
    }

    public Path getDir() {
        return dir;
    }

    public int getType() {
        return type;
    }

    public boolean isDirectDep() {
        return type == DIRECT_DEP;
    }

    public boolean isTransitiveDep() {
        return type == TRANSITIVE_DEP;
    }

    public boolean isPatch() {
        return type == PATCH;
    }

    /**
     * Returns a resource path for a feature-pack.
     *
     * @param path  path to the resource relative to the feature-pack resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack was not found in the layout
     */
    public Path getResource(String... path) throws ProvisioningDescriptionException {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return dir.resolve(Constants.RESOURCES).resolve(path[0]);
        }
        Path p = dir.resolve(Constants.RESOURCES);
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    public boolean hasFeatureSpec(String name) {
        return Files.exists(dir.resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML));
    }

    public FeatureSpec loadFeatureSpec(String name) throws ProvisioningException {
        final Path specXml = dir.resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML);
        if (!Files.exists(specXml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(specXml)) {
            return FeatureSpecXmlParser.getInstance().parse(reader);
        } catch (Exception e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(specXml), e);
        }
    }

    public ConfigLayerSpec loadConfigLayerSpec(String model, String name) throws ProvisioningException {
        final Path specXml;
        if (model == null) {
            specXml = getDir().resolve(Constants.LAYERS).resolve(name).resolve(Constants.LAYER_SPEC_XML);
        } else {
            specXml = getDir().resolve(Constants.LAYERS).resolve(model).resolve(name).resolve(Constants.LAYER_SPEC_XML);
        }
        if (!Files.exists(specXml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(specXml)) {
            return ConfigLayerSpecXmlParser.getInstance().parse(reader);
        } catch (Exception e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(specXml), e);
        }
    }

    public Set<ConfigId> loadLayers() throws ProvisioningException, IOException {
        Path layersDir = getDir().resolve(Constants.LAYERS);
        if (!Files.exists(layersDir)) {
            return Collections.emptySet();
        }
        Set<ConfigId> layers = new HashSet<>();

        Files.walkFileTree(layersDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals(Constants.LAYER_SPEC_XML)) {
                    ConfigId id;
                    Path rootDir = file.getParent().getParent();
                    // No model
                    if (rootDir.equals(layersDir)) {
                        id = new ConfigId(null, file.getParent().getFileName().toString());
                    } else {
                        id = new ConfigId(rootDir.getFileName().toString(),
                                file.getParent().getFileName().toString());
                    }
                    layers.add(id);
                }
                return FileVisitResult.CONTINUE;
            }

        });
        return layers;
    }

}