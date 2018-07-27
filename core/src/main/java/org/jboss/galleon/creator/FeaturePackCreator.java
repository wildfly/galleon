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
import java.util.List;
import java.util.Map;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackCreator extends UniverseResolverBuilder<FeaturePackCreator> {

    private static final String BUILD = "build";
    private static final String GLN_FP_INSTALLER = "gln-fp-installer";

    public static FeaturePackCreator getInstance() {
        return new FeaturePackCreator();
    }

    private Map<String, UniverseFeaturePackInstaller> ufpInstallers;
    private List<FeaturePackBuilder> fps = Collections.emptyList();
    private Path workDir;
    private Path buildDir;
    private UniverseResolver universeResolver;
    private boolean universeResolution = true;
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
        ufpInstallers = UniverseFeaturePackInstaller.load();
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

    public void install(Path dir) throws ProvisioningException {
        try {
            universeResolution = false;
            buildDir = dir;
            for (FeaturePackBuilder fp : fps) {
                fp.build();
            }
        } finally {
            buildDir = null;
            universeResolution = true;
            if (workDir != null) {
                IoUtils.recursiveDelete(workDir);
            }
        }
    }

    void install(FeaturePackLocation.FPID fpid, Path fpContentDir) throws ProvisioningException {
        Universe<?> universe = null;
        UniverseFeaturePackInstaller ufpInstaller = null;
        if (universeResolution) {
            universe = universeResolver.getUniverse(fpid.getLocation().getUniverse());
            ufpInstaller = ufpInstallers.get(universe.getFactoryId());
            if (ufpInstaller == null) {
                throw new ProvisioningException(Errors.featurePackInstallerNotFound(universe.getFactoryId(), ufpInstallers.keySet()));
            }
        }
        final Path fpZip = getBuildDir().resolve(LayoutUtils.ensureValidFileName(fpid.toString()));
        try {
            ZipUtils.zip(fpContentDir, fpZip);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to create feature-pack archive", e);
        }
        if (ufpInstaller != null) {
            ufpInstaller.install(universe, fpid, fpZip);
        }
    }

    Path getWorkDir() throws ProvisioningException {
        if(workDir == null) {
            try {
                workDir = Files.createTempDirectory(GLN_FP_INSTALLER);
            } catch (IOException e) {
                throw new ProvisioningException("Failed to create a tmp dir");
            }
        }
        return workDir;
    }

    private Path getBuildDir() throws ProvisioningException {
        if(buildDir != null) {
            return buildDir;
        }
        try {
            return Files.createDirectories(getWorkDir().resolve(BUILD));
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(getWorkDir().resolve(BUILD)));
        }
    }
}
