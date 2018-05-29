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
package org.jboss.galleon.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author Alexey Loubyansky
 */
public class LayoutUtils {

    public static Path getFeaturePackDir(Path fpLayoutDir, FPID fpid) throws ProvisioningDescriptionException {
        return getFeaturePackDir(fpLayoutDir, fpid, true);
    }

    public static Path getFeaturePackDir(Path fpLayoutDir, FPID fpid, boolean existing) throws ProvisioningDescriptionException {
        final FeaturePackLocation fps = fpid.getLocation();
        final Path fpPath = fpLayoutDir.resolve(fps.getUniverse().getFactory()).resolve(fps.getProducer()).resolve(fps.getChannelName()).resolve(fpid.getBuild());
        if(existing && !Files.exists(fpPath)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpPath));
        }
        return fpPath;
    }

    public static Path getPackageDir(Path fpDir, String packageName) throws ProvisioningDescriptionException {
        return getPackageDir(fpDir, packageName, true);
    }

    public static Path getPackageDir(Path fpDir, String packageName, boolean existing) throws ProvisioningDescriptionException {
        final Path dir = fpDir.resolve(Constants.PACKAGES).resolve(packageName);
        if(existing && !Files.exists(dir)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(dir));
        }
        return dir;
    }

    public static Path getPackageContentDir(Path fpDir, String packageName) {
        return fpDir.resolve(Constants.PACKAGES).resolve(packageName).resolve(Constants.CONTENT);
    }
}
