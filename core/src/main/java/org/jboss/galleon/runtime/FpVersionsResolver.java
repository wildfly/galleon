/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FpVersionsResolver {

    static void resolveFpVersions(ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        new FpVersionsResolver(rt).assertVersions();
    }

    static Map<ArtifactCoords.Ga, ArtifactCoords.Gav> resolveDeps(ProvisioningRuntimeBuilder rt, ArtifactCoords.Gav fpGav) throws ProvisioningException {
        return resolveDeps(rt, rt.getOrLoadFpBuilder(fpGav).spec, Collections.emptyMap());
    }

    static Map<ArtifactCoords.Ga, ArtifactCoords.Gav> resolveDeps(ProvisioningRuntimeBuilder rt, FeaturePackDepsConfig fpDeps, Map<ArtifactCoords.Ga, ArtifactCoords.Gav> collected)
            throws ProvisioningException {
        if(!fpDeps.hasFeaturePackDeps()) {
            return collected;
        }
        for(FeaturePackConfig fpConfig : fpDeps.getFeaturePackDeps()) {
            final int size = collected.size();
            collected = CollectionUtils.put(collected, fpConfig.getGav().toGa(), fpConfig.getGav());
            if(size == collected.size()) {
                continue;
            }
            collected = resolveDeps(rt, rt.getOrLoadFpBuilder(fpConfig.getGav()).spec, collected);
        }
        return collected;
    }

    private final ProvisioningRuntimeBuilder rt;
    private Set<ArtifactCoords.Ga> missingVersions = Collections.emptySet();
    private List<ArtifactCoords.Ga> branch = new ArrayList<>();
    private Map<ArtifactCoords.Ga, Set<ArtifactCoords.Gav>> conflicts = Collections.emptyMap();
    private Map<ArtifactCoords.Ga, FeaturePackRuntimeBuilder> loaded = Collections.emptyMap();

    private FpVersionsResolver(ProvisioningRuntimeBuilder rt) {
        this.rt = rt;
    }

    public boolean hasMissingVersions() {
        return !missingVersions.isEmpty();
    }

    public Set<ArtifactCoords.Ga> getMissingVersions() {
        return missingVersions;
    }

    public boolean hasVersionConflicts() {
        return !conflicts.isEmpty();
    }

    public Map<ArtifactCoords.Ga, Set<ArtifactCoords.Gav>> getVersionConflicts() {
        return conflicts;
    }

    private void assertVersions() throws ProvisioningException {
        assertVersions(rt.config);
        if(!missingVersions.isEmpty() || !conflicts.isEmpty()) {
            throw new ProvisioningDescriptionException(Errors.fpVersionCheckFailed(missingVersions, conflicts.values()));
        }
    }

    private void assertVersions(FeaturePackDepsConfig fpDepsConfig) throws ProvisioningException {
        if(!fpDepsConfig.hasFeaturePackDeps()) {
            return;
        }
        final int branchSize = branch.size();
        final Collection<FeaturePackConfig> fpDeps = fpDepsConfig.getFeaturePackDeps();
        Set<ArtifactCoords.Gav> skip = Collections.emptySet();
        for(FeaturePackConfig fpConfig : fpDeps) {
            final Gav gav = fpConfig.getGav();
            if(gav.getVersion() == null) {
                missingVersions = CollectionUtils.addLinked(missingVersions, gav.toGa());
                continue;
            }
            final FeaturePackRuntimeBuilder fp = loaded.get(gav.toGa());
            if(fp != null) {
                if(!fp.gav.equals(gav) && !branch.contains(gav.toGa())) {
                    Set<Gav> versions = conflicts.get(fp.gav.toGa());
                    if(versions != null) {
                        versions.add(gav);
                        continue;
                    }
                    versions = new LinkedHashSet<ArtifactCoords.Gav>();
                    versions.add(fp.gav);
                    versions.add(gav);
                    conflicts = CollectionUtils.putLinked(conflicts, gav.toGa(), versions);
                }
                skip = CollectionUtils.add(skip, fp.gav);
                continue;
            }
            load(gav);
            if(!missingVersions.isEmpty()) {
                missingVersions = CollectionUtils.remove(missingVersions, gav.toGa());
            }
            branch.add(gav.toGa());
        }
        for(FeaturePackConfig fpConfig : fpDeps) {
            final Gav gav = fpConfig.getGav();
            if(gav.getVersion() == null || skip.contains(gav)) {
                continue;
            }
            assertVersions(rt.getFpBuilder(gav, true).spec);
        }
        for(int i = 0; i < branch.size() - branchSize; ++i) {
            branch.remove(branch.size() - 1);
        }
    }

    private FeaturePackRuntimeBuilder load(ArtifactCoords.Gav gav) throws ProvisioningException {
        final FeaturePackRuntimeBuilder fp = rt.getOrLoadFpBuilder(gav);
        loaded = CollectionUtils.put(loaded, gav.toGa(), fp);
        return fp;
    }
}
