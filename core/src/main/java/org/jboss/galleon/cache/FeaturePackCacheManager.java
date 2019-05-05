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
package org.jboss.galleon.cache;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.ZipUtils;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeaturePackCacheManager implements Closeable {

    public interface OverwritePolicy {

        boolean hasExpired(Path fpDir, FPID fpid);

        void cached(FPID fpid);
    }

    private static class DefaultOverwritePolicy implements OverwritePolicy {

        @Override
        public boolean hasExpired(Path fpDir, FPID fpid) {
            return false;
        }

        @Override
        public void cached(FPID fpid) {
        }
    }

    private final Path home;
    private final OverwritePolicy overwritePolicy;

    public FeaturePackCacheManager() {
        this(null, null);
    }

    public FeaturePackCacheManager(Path home) {
        this(home, null);
    }

    public FeaturePackCacheManager(Path home, OverwritePolicy overwritePolicy) {
        this.home = home == null ? IoUtils.createRandomTmpDir() : home;
        this.overwritePolicy = overwritePolicy == null ? new DefaultOverwritePolicy() : overwritePolicy;
    }

    public Path put(UniverseResolver universeResolver, FeaturePackLocation fpl) throws ProvisioningException {
        Path fpDir = LayoutUtils.getFeaturePackDir(home, fpl.getFPID(), false);
        if (Files.exists(fpDir)) {
            if (!overwritePolicy.hasExpired(fpDir, fpl.getFPID())) {
                return fpDir;
            }
            IoUtils.recursiveDelete(fpDir);
        }
        unpack(fpDir, universeResolver.resolve(fpl));
        overwritePolicy.cached(fpl.getFPID());
        return fpDir;
    }

    public Path put(Path featurePack, FeaturePackLocation.FPID fpid) throws ProvisioningException {
        final Path fpDir = LayoutUtils.getFeaturePackDir(home, fpid, false);
        if (Files.exists(fpDir)) {
            IoUtils.recursiveDelete(fpDir);
        }

        unpack(fpDir, featurePack);
        overwritePolicy.cached(fpid);
        return fpDir;
    }

    public void remove(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        IoUtils.recursiveDelete(LayoutUtils.getFeaturePackDir(home, fpid, false));
    }

    @Override
    public void close() {
        IoUtils.recursiveDelete(home);
    }

    public Path getHome() {
        return home;
    }

    private void unpack(final Path fpDir, final Path artifactPath) throws ProvisioningException {
        try {
            Files.createDirectories(fpDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(fpDir), e);
        }
        try {
            ZipUtils.unzip(artifactPath, fpDir);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to unzip " + artifactPath + " to " + fpDir, e);
        }
    }
}
