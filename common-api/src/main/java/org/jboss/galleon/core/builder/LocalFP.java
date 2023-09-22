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
