/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.impl;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jboss.galleon.Constants;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.APIVersion;
import org.jboss.galleon.api.GalleonFeaturePackDescription;
import org.jboss.galleon.api.GalleonFeaturePack;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.ZipUtils;

public class ProvisioningUtil {

    public static boolean isFeaturePack(Path path) {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("galleon-tmp");
            Path spec = getFeaturePackSpec(path, tmp);
            return Files.exists(spec);
        } catch (Exception ex) {
            return false;
        } finally {
            IoUtils.recursiveDelete(tmp);
        }
    }

    public static FPID getFeaturePackProducer(Path path) throws Exception {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("galleon-tmp");
            Path spec = getFeaturePackSpec(path, tmp);
            GalleonFeaturePackDescription deps = FeaturePackLightXmlParser.parseDescription(spec);
            return deps.getProducer();
        } finally {
            IoUtils.recursiveDelete(tmp);
        }
    }

    public static String getCoreVersion(Path resolvedFP, String currentVersion, Path tmp, UniverseResolver universeResolver) throws Exception {
        Path spec = getFeaturePackSpec(resolvedFP, tmp);
        String fpVersion = FeaturePackLightXmlParser.parseVersion(spec);
        //System.out.println("Found a version in FP " + resolvedFP + " version is " + fpVersion);
        if (fpVersion != null && !fpVersion.isEmpty()) {
            if (VersionMatcher.COMPARATOR.compare(fpVersion, currentVersion) > 0) {
                currentVersion = fpVersion;
            }
        }
        return currentVersion;
    }

    public static String getMavenCoords(GalleonFeaturePack fp) {
        return fp.getMavenCoords();
    }

    public static Class<?> getCallerClass(URLClassLoader loader) throws ProvisioningException {
        try {
            return Class.forName("org.jboss.galleon.caller.ProvisioningContextBuilderImpl", true, loader);
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }

    public static String getCoreVersion(Path provisioning, UniverseResolver universeResolver, Path tmp) throws ProvisioningException {
        List<FPID> featurePacks = ProvisioningLightXmlParser.parse(provisioning);
        return getCoreVersion(featurePacks, APIVersion.getVersion(), universeResolver, tmp);
    }

    public static String getCoreVersion(InputStream stream, UniverseResolver universeResolver, Path tmp) throws ProvisioningException {
        List<FPID> featurePacks = ProvisioningLightXmlParser.parse(stream);
        return getCoreVersion(featurePacks, APIVersion.getVersion(), universeResolver, tmp);
    }

    private static String getCoreVersion(List<FPID> featurePacks, String currentMax, UniverseResolver universeResolver, Path tmp) throws ProvisioningException {
        try {
            String version = currentMax;
            for (FPID fpid : featurePacks) {
                Path resolvedFP = universeResolver.resolve(fpid.getLocation());
                Path spec = getFeaturePackSpec(resolvedFP, tmp);
                String fpVersion = FeaturePackLightXmlParser.parseVersion(spec);
                //System.out.println("Found a version in FP " + fpid + " version is " + fpVersion);
                if (fpVersion != null && !fpVersion.isEmpty()) {
                    if (VersionMatcher.COMPARATOR.compare(fpVersion, version) > 0) {
                        version = fpVersion;
                    }
                }
            }
            return version;
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }

    public static GalleonFeaturePackDescription getFeaturePackDescription(Path fp) throws ProvisioningException {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("galleon-tmp");
            Path spec = getFeaturePackSpec(fp, tmp);
            return FeaturePackLightXmlParser.parseDescription(spec);
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        } finally {
            IoUtils.recursiveDelete(tmp);
        }
    }

    private static Path getFeaturePackSpec(Path resolvedFP, Path tmp) throws Exception {
        Path fpDir = tmp.resolve(resolvedFP.getFileName());
        Files.createDirectories(fpDir);
        Path target = fpDir.resolve("fp-spec.xml");
        if (!Files.exists(target)) {
            try (FileSystem fs = ZipUtils.newFileSystem(resolvedFP)) {
                Path spec = fs.getPath(Constants.FEATURE_PACK_XML);
                ZipUtils.copyFromZip(spec, target);
            }
        }
        return target;
    }
}
