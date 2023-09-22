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
package org.jboss.galleon.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.core.builder.LocalFP;
import org.jboss.galleon.impl.ProvisioningUtil;
import org.jboss.galleon.repo.RepositoryArtifactResolver;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseResolverBuilder;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author jdenise
 */
public class GalleonBuilder extends UniverseResolverBuilder<GalleonBuilder> {

    private static final String GALLEON_CORE_GROUP_ID = "org.jboss.galleon";
    private static final String GALLEON_CORE_ARTIFACT_ID = "galleon-core";

    private static class ClassLoaderUsage {

        int num = 1;
        URLClassLoader loader;
    }
    private static final Map<String, ClassLoaderUsage> classLoaders = new HashMap<>();
    private UniverseResolver resolver;
    private final Map<FeaturePackLocation.FPID, LocalFP> locals = new HashMap<>();

    public FeaturePackLocation addLocal(Path path, boolean installInUniverse) throws ProvisioningException {
        final FeaturePackLocation.FPID fpid;
        try {
            fpid = ProvisioningUtil.getFeaturePackProducer(path);
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
        locals.put(fpid, new LocalFP(fpid, path, installInUniverse));
        return fpid.getLocation();
    }

    public GalleonBuilder setUniverseResolver(UniverseResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    private UniverseResolver getUniverseResolver() throws ProvisioningException {
        if (resolver == null) {
            resolver = buildUniverseResolver();
        }
        return resolver;
    }

    public ProvisioningBuilder newProvisioningBuilder() throws ProvisioningException {
        String coreVersion = getMavenResolvedCoreVersion();
        if (coreVersion == null) {
            coreVersion = APIVersion.getVersion();
        }
        return new ProvisioningBuilder(getUniverseResolver(), locals, coreVersion);
    }

    public ProvisioningBuilder newProvisioningBuilder(Path provisioning) throws ProvisioningException {
        // First identify if the core version is ruled by MavenRepoManager
        String coreVersion = getMavenResolvedCoreVersion();
        if (coreVersion == null) {
            Path tmp = getTmpDirectory();
            try {
                coreVersion = ProvisioningUtil.getCoreVersion(provisioning, getUniverseResolver(), tmp);
                checkArtifactResolver(coreVersion, getUniverseResolver());
            } finally {
                IoUtils.recursiveDelete(tmp);
            }
        }
        return new ProvisioningBuilder(getUniverseResolver(), locals, coreVersion);
    }

    public ProvisioningBuilder newProvisioningBuilder(GalleonProvisioningConfig config) throws ProvisioningException {
        return new ProvisioningBuilder(getUniverseResolver(), locals, getCoreVersion(config));
    }

    public URLClassLoader getCoreClassLoader(Path file) throws ProvisioningException {
        return getCallerClassLoader(getCoreVersion(file), resolver);
    }

    public URLClassLoader getCoreClassLoader(String coreVersion) throws ProvisioningException {
                // First identify if the core version is ruled by MavenRepoManager
        String mavenResolvedVersion = getMavenResolvedCoreVersion();
        if(mavenResolvedVersion != null) {
            coreVersion = mavenResolvedVersion;
        }
        return getCallerClassLoader(coreVersion, resolver);
    }

    public String getCoreVersion(FeaturePackLocation fpl) throws ProvisioningException {
        GalleonProvisioningConfig config = GalleonProvisioningConfig.builder().addFeaturePackDep(fpl).build();
        return getCoreVersion(config);
    }

    public String getCoreVersion(Path file) throws ProvisioningException {
        String coreVersion = getMavenResolvedCoreVersion();
        if (coreVersion == null) {
            Path tmp = getTmpDirectory();
            try {
                coreVersion = ProvisioningUtil.getCoreVersion(file, getUniverseResolver(), tmp);
                checkArtifactResolver(coreVersion, getUniverseResolver());
            } finally {
                IoUtils.recursiveDelete(tmp);
            }
        }
        return coreVersion;

    }

    private String getMavenResolvedCoreVersion() throws ProvisioningException {
        String coreVersion = null;
        UniverseResolver universeResolver = getUniverseResolver();
        if (hasMavenArtifactResolver(universeResolver)) {
            RepositoryArtifactResolver repoManager = getArtifactResolver(universeResolver);
            if (repoManager instanceof MavenStreamResolver) {
                MavenStreamResolver resolver = (MavenStreamResolver) repoManager;
                try {
                    coreVersion = resolver.getLatestVersion(GALLEON_CORE_GROUP_ID, GALLEON_CORE_ARTIFACT_ID, null, null, null);
                } catch(Exception ex) {
                    // XXX OK, not resolvable.
                }
            }
        }
        return coreVersion;
    }
    private String getCoreVersion(GalleonProvisioningConfig config) throws ProvisioningException {
        // First identify if the core version is ruled by MavenRepoManager
        String mavenResolvedVersion = getMavenResolvedCoreVersion();
        if(mavenResolvedVersion != null) {
            return mavenResolvedVersion;
        }
        Path tmp = getTmpDirectory();
        try {
            String coreVersion = APIVersion.getVersion();
            for (GalleonFeaturePackConfig fp : config.getFeaturePackDeps()) {
                LocalFP local = locals.get(fp.getLocation().getFPID());
                Path resolvedFP;
                if (local == null) {
                    resolvedFP = getUniverseResolver().resolve(fp.getLocation());
                } else {
                    resolvedFP = local.getPath();
                }
                try {
                    coreVersion = ProvisioningUtil.getCoreVersion(resolvedFP, coreVersion, tmp, getUniverseResolver());
                } catch (Exception ex) {
                    throw new ProvisioningException(ex);
                }
            }
            checkArtifactResolver(coreVersion, getUniverseResolver());
            return coreVersion;
        } finally {
            IoUtils.recursiveDelete(tmp);
        }
    }

    private static void checkArtifactResolver(String coreVersion, UniverseResolver universeResolver) throws ProvisioningException {
        if (!APIVersion.getVersion().equals(coreVersion)) {
            // Check that we will be able to resolve the core artifact
            try {
                getArtifactResolver(universeResolver);
            } catch (ProvisioningException ex) {
                throw new ProvisioningException("No maven artifact resolver specified in universe, "
                        + "the Galleon core library can't be resolved");
            }
        }
    }

    private static RepositoryArtifactResolver getArtifactResolver(UniverseResolver universeResolver) throws ProvisioningException {
        return universeResolver.getArtifactResolver(MavenRepoManager.REPOSITORY_ID);
    }

    private static boolean hasMavenArtifactResolver(UniverseResolver universeResolver) throws ProvisioningException {
        return universeResolver.hasArtifactResolver(MavenRepoManager.REPOSITORY_ID);
    }

    private static Path getTmpDirectory() throws ProvisioningException {
        try {
            return Files.createTempDirectory("galleon-tmp");
        } catch (IOException ex) {
            throw new ProvisioningException(ex);
        }
    }

    static synchronized void releaseUsage(String version) throws ProvisioningException {
        ClassLoaderUsage usage = classLoaders.get(version);
        if (usage == null) {
            throw new ProvisioningException("Releasing usage of core " + version + " although no usage");
        }
        if (usage.num <= 0) {
            throw new ProvisioningException("Releasing usage of core " + version + " although all usages released");
        }
        usage.num -= 1;
        if (usage.num == 0) {
            try {
                usage.loader.close();
            } catch (IOException ex) {
                throw new ProvisioningException(ex);
            }
            classLoaders.remove(version);
        }
    }

    static Map<String, ClassLoaderUsage> getClassLoaders() {
        return Collections.unmodifiableMap(classLoaders);
    }

    private static synchronized ClassLoaderUsage addDefaultCoreClassLoader() throws ProvisioningException {
        String apiVersion = APIVersion.getVersion();
        try {
            Path corePath = Files.createTempDirectory("galleon-core-default-base-dir");
            corePath.toFile().deleteOnExit();
            // Handle local core
            File defaultCore = corePath.resolve("galleon-core.jar").toFile();
            try (InputStream input = ProvisioningImpl.class.getClassLoader().getResourceAsStream("galleon-core-" + apiVersion + ".jar")) {
                try (OutputStream output = new FileOutputStream(defaultCore, false)) {
                    input.transferTo(output);
                }
            }
            defaultCore.deleteOnExit();
            URL[] cp = new URL[1];
            ClassLoaderUsage usage = new ClassLoaderUsage();
            try {
                cp[0] = defaultCore.toURI().toURL();
                usage.loader = new URLClassLoader(cp, Thread.currentThread().getContextClassLoader());
            } catch (Exception ex) {
                throw new ProvisioningException(ex);
            }
            classLoaders.put(apiVersion, usage);
            return usage;
        } catch (IOException ex) {
            throw new ProvisioningException(ex);
        }
    }

    static synchronized URLClassLoader getCallerClassLoader(String version, UniverseResolver universeResolver) throws ProvisioningException {
        ClassLoaderUsage usage = classLoaders.get(version);
        if (usage == null) {
            //System.out.println("NEW USAGE OF " + version);
            if (APIVersion.getVersion().equals(version)) {
                usage = addDefaultCoreClassLoader();
            } else {
                RepositoryArtifactResolver repoManager = (RepositoryArtifactResolver) universeResolver.getArtifactResolver(MavenRepoManager.REPOSITORY_ID);
                usage = new ClassLoaderUsage();
                classLoaders.put(version, usage);
                String loc = GALLEON_CORE_GROUP_ID + ":" + GALLEON_CORE_ARTIFACT_ID + ":jar:" + version;
                Path path;
                try {
                    path = repoManager.resolve(loc);
                } catch (MavenUniverseException ex) {
                    throw new ProvisioningException(ex);
                }

                URL[] cp = new URL[1];
                try {
                    cp[0] = path.toFile().toURI().toURL();
                    usage.loader = new URLClassLoader(cp, Thread.currentThread().getContextClassLoader());
                } catch (Exception ex) {
                    throw new ProvisioningException(ex);
                }
            }
        } else {
            //System.out.println("REUSE OF " + version);
            usage.num += 1;
        }
        return usage.loader;
    }
}
