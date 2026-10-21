/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util;

import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.galleon.BaseErrors;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;

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
        final UniverseSpec universe = fps.getUniverse();
        Path fpPath = fpLayoutDir.resolve(universe.getFactory());
        if (universe.getLocation() != null) {
            fpPath = fpPath.resolve(ensureValidFileName(universe.getLocation()));
        }
        fpPath = fpPath.resolve(ensureValidFileName(fps.getProducerName()));
        if(fps.getChannelName() != null) {
            fpPath = fpPath.resolve(fps.getChannelName());
        }
        fpPath = fpPath.resolve(ensureValidFileName(fpid.getBuild()));
        if (existing && !Files.exists(fpPath)) {
            throw new ProvisioningDescriptionException(BaseErrors.pathDoesNotExist(fpPath));
        }
        return fpPath;
    }

    public static String ensureValidFileName(String value) {
        // replace characters that are invalid in paths
        return value.replaceAll("[:\\(\\)\\[\\]\\,]", "_");
    }

    public static Path getPackageDir(Path fpDir, String packageName) throws ProvisioningDescriptionException {
        return getPackageDir(fpDir, packageName, true);
    }

    public static Path getPackageDir(Path fpDir, String packageName, boolean existing) throws ProvisioningDescriptionException {
        final Path dir = fpDir.resolve(Constants.PACKAGES).resolve(packageName);
        if(existing && !Files.exists(dir)) {
            throw new ProvisioningDescriptionException(BaseErrors.pathDoesNotExist(dir));
        }
        return dir;
    }

    public static Path getPackageContentDir(Path fpDir, String packageName) {
        return fpDir.resolve(Constants.PACKAGES).resolve(packageName).resolve(Constants.CONTENT);
    }

    public static Path getLayerSpecXml(Path fpDir, String model, String name, boolean existing) throws ProvisioningDescriptionException {
        Path p = fpDir.resolve(Constants.LAYERS);
        if(model != null) {
            p = p.resolve(model);
        }
        p = p.resolve(name).resolve(Constants.LAYER_SPEC_XML);
        if(existing && !Files.exists(p)) {
            throw new ProvisioningDescriptionException("Failed to locate XML file describing configuration layer " + name + " at " + p);
        }
        return p;
    }

    public static Path getConfigXml(Path fpDir, ConfigId configId, boolean existing) throws ProvisioningDescriptionException {
        final String model = configId.getModel();
        final String name = configId.getName();
        final Path p;
        if(name == null) {
            if(model == null) {
                throw new ProvisioningDescriptionException("Anonymous configs are included in feature-pack.xml");
            }
            p = fpDir.resolve(Constants.CONFIGS).resolve(model).resolve(Constants.MODEL_XML);
        } else if(model == null) {
            p = fpDir.resolve(Constants.CONFIGS).resolve(name).resolve(Constants.CONFIG_XML);
        } else {
            p = fpDir.resolve(Constants.CONFIGS).resolve(model).resolve(name).resolve(Constants.CONFIG_XML);
        }
        if(existing && !Files.exists(p)) {
            throw new ProvisioningDescriptionException("Failed to locate XML file describing configuration " + configId + " at " + p);
        }
        return p;
    }

    public static Path getHashesDir(Path home) {
        return home.resolve(Constants.PROVISIONED_STATE_DIR).resolve(Constants.HASHES);
    }
}
