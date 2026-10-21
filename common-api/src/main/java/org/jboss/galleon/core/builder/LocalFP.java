/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.core.builder;

import java.nio.file.Path;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise
 */
public class LocalFP {

    private final FeaturePackLocation.FPID fpid;
    private Path path;
    private final boolean installInUniverse;

    public LocalFP(FeaturePackLocation.FPID fpid, Path path, boolean installInUniverse) {
        this.fpid = fpid;
        this.path = path;
        this.installInUniverse = installInUniverse;
    }

    /**
     * @return the fpid
     */
    public FeaturePackLocation.FPID getFPID() {
        return fpid;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return the installInUniverse
     */
    public boolean isInstallInUniverse() {
        return installInUniverse;
    }
}
