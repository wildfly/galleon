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
package org.jboss.galleon.cli.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.runtime.FeaturePackRuntime;
import org.jboss.galleon.runtime.ResolvedFeatureSpec;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureSpecsBuilder {

    private final Map<ResolvedSpecId, FeatureSpecInfo> allspecs = new HashMap<>();

    public Map<ResolvedSpecId, FeatureSpecInfo> getAllSpecs() {
        return allspecs;
    }

    public Group buildTree(ProvisioningLayout<FeaturePackRuntime> layout, PmSession session, FPID fpid, FPID id,
            Map<Identity, Group> allPackages, boolean useCache, Set<ResolvedSpecId> wantedSpecs) throws IOException, ProvisioningException {
        // Build the tree of specs located in all feature-packs
        FeatureGroupsBuilder grpBuilder = new FeatureGroupsBuilder();
        Set<FeatureSpecInfo> specs = new HashSet<>();
        for (ResolvedFeatureSpec resolvedSpec : layout.getFeaturePack(fpid.getProducer()).getFeatureSpecs()) {
            ResolvedSpecId resolved = resolvedSpec.getId();
            if (wantedSpecs == null || wantedSpecs.contains(resolved)) {
                FeatureSpecInfo specInfo = allspecs.get(resolved);
                if (specInfo == null) {
                    Set<Identity> missingPackages = new HashSet<>();
                    FeatureSpec spec = resolvedSpec.getSpec();
                    specInfo = new FeatureSpecInfo(resolved, id, spec);
                    Identity specId = Identity.fromChannel(resolved.getProducer(), resolved.getName());
                    boolean featureEnabled = true;
                    for (PackageDependencySpec p : spec.getLocalPackageDeps()) {
                        Identity identity = Identity.fromChannel(resolved.getProducer(), p.getName());
                        Group grp = allPackages.get(identity);
                        // Group can be null if the modules have not been installed.
                        if (grp != null) {
                            specInfo.addPackage(grp.getPackage());
                            attachProvider(specId, grp, new HashSet<>());
                        } else {
                            featureEnabled = false;
                            missingPackages.add(identity);
                        }
                    }
                    for (String o : spec.getPackageOrigins()) {
                        for (PackageDependencySpec p : spec.getExternalPackageDeps(o)) {
                            Identity identity = Identity.fromString(o, p.getName());
                            Group grp = allPackages.get(identity);
                            if (grp != null) {
                                specInfo.addPackage(grp.getPackage());
                                attachProvider(specId, grp, new HashSet<>());
                            } else {
                                featureEnabled = false;
                                missingPackages.add(identity);
                            }
                        }
                    }
                    specInfo.setEnabled(featureEnabled);
                    specInfo.setMissingPackages(missingPackages);
                    allspecs.put(resolved, specInfo);
                    specs.add(specInfo);
                }

                String fullSpecName = resolved.getName();
                List<String> path = new ArrayList<>();
                Group parent = grpBuilder.buildFeatureSpecGroups(fullSpecName, specInfo, path);
                parent.setFeatureSpec(specInfo);
            }
        }
        return grpBuilder.getRoot();
    }

    private static void attachProvider(Identity provider, Group grp, HashSet<Group> seen) {
        grp.getPackage().addProvider(provider);
        if (seen.contains(grp)) {
            return;
        }
        seen.add(grp);
        for (Group dep : grp.getGroups()) {
            attachProvider(provider, dep, seen);
        }
    }
}
