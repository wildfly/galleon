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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureSpecInfo {

    private final ResolvedSpecId specId;
    private final FPID currentFP;
    private final FeatureSpec spec;

    private final List<PackageInfo> packages = new ArrayList<>();

    private boolean enabled;
    private Set<Identity> missingPackages;

    private String name;

    public FeatureSpecInfo(ResolvedSpecId specId, FPID currentFP, FeatureSpec spec) {
        this.specId = specId;
        this.currentFP = currentFP;
        this.spec = spec;
    }

    // When attached to a group.
    void setName(String name) {
        this.name = name;
    }

    Set<String> getAllParameters() {
        return spec.getParams().keySet();
    }

    void addPackage(PackageInfo pkg) {
        packages.add(pkg);
    }

    public String getName() {
        return name;
    }

    public List<PackageInfo> getPackages() {
        return packages;
    }

    public String getType() {
        return spec.getName();
    }

    public FeatureSpec getSpec() {
        return spec;
    }

//    public Map<String, FeatureParameterSpec> getParameters() {
//        Map<String, FeatureParameterSpec> res = new HashMap<>();
//        for (Entry<String, FeatureParameterSpec> entry : spec.getParams().entrySet()) {
//            if (!spec.getIdParams().contains(entry.getValue())) {
//                res.put(entry.getKey(), entry.getValue());
//            }
//        }
//        return res;
//    }

    public String getDescription() {
        return "no description available";
    }

    public ResolvedSpecId getSpecId() {
        return specId;
    }

    public String getFeatureSpecOrigin() {
        return Identity.buildOrigin(specId.getProducer());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FeatureSpecInfo)) {
            return false;
        }
        FeatureSpecInfo ofs = (FeatureSpecInfo) other;
        return specId.equals(ofs.specId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.specId);
        return hash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean featureEnabled) {
        enabled = featureEnabled;
    }

    void setMissingPackages(Set<Identity> missingPackages) {
        this.missingPackages = missingPackages;
    }

    public Set<Identity> getMissingPackages() {
        return missingPackages;
    }
}
