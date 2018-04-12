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
package org.jboss.galleon.cli;

import java.nio.file.Paths;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class Demo {

    public static void main(String[] args) throws Exception {

        final ProvisioningManager pm = ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(Paths.get("/home/olubyans/demo/wf"))
                .build();

        // pm.install(ArtifactCoords.getGavPart("org.wildfly.core", "wildfly-core-feature-pack-new", "3.0.0.Alpha9-SNAPSHOT"));
        pm.provision(ProvisioningConfig.builder()
                .addFeaturePackDep(
                        FeaturePackConfig
                                .builder(ArtifactCoords.newGav("org.wildfly.core", "wildfly-core-feature-pack-new", "3.0.0.Alpha9-SNAPSHOT"))
                                .excludePackage("org.jboss.as.deployment-scanner.main")
                                .excludePackage("docs")
                                .build())
                .addFeaturePackDep(ArtifactCoords.newGav("org.wildfly", "wildfly-servlet-feature-pack-new", "11.0.0.Alpha1-SNAPSHOT"))
                .build());
    }
}
