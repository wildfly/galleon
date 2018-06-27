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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.FeaturePackLocation.ChannelSpec;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FpVersionsResolver {

    static void resolveFpVersions(ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        new FpVersionsResolver(rt).assertVersions();
    }

    static Map<ChannelSpec, FPID> resolveDeps(ProvisioningRuntimeBuilder rt, FeaturePackRuntimeBuilder fp, Map<ChannelSpec, FPID> collected)
            throws ProvisioningException {
        if(!fp.spec.hasFeaturePackDeps()) {
            return collected;
        }
        for(FeaturePackConfig fpConfig : fp.spec.getFeaturePackDeps()) {
            final int size = collected.size();
            collected = CollectionUtils.put(collected, fpConfig.getLocation().getChannel(), fpConfig.getLocation().getFPID());
            if(size == collected.size()) {
                continue;
            }
            collected = resolveDeps(rt, rt.getOrLoadFpBuilder(fpConfig.getLocation().getFPID()), collected);
        }
        return collected;
    }

    private final ProvisioningRuntimeBuilder rt;
    private Set<ChannelSpec> missingVersions = Collections.emptySet();
    private List<ChannelSpec> branch = new ArrayList<>();
    private Map<ChannelSpec, Set<FPID>> conflicts = Collections.emptyMap();
    private Map<ChannelSpec, FeaturePackRuntimeBuilder> loaded = Collections.emptyMap();

    private FpVersionsResolver(ProvisioningRuntimeBuilder rt) {
        this.rt = rt;
    }

    public boolean hasMissingVersions() {
        return !missingVersions.isEmpty();
    }

    public Set<ChannelSpec> getMissingVersions() {
        return missingVersions;
    }

    public boolean hasVersionConflicts() {
        return !conflicts.isEmpty();
    }

    public Map<ChannelSpec, Set<FPID>> getVersionConflicts() {
        return conflicts;
    }

    private void assertVersions() throws ProvisioningException {
        assertVersions(rt.config);

        if(!missingVersions.isEmpty() && conflicts.isEmpty()) {
            final ProvisioningConfig.Builder builder = ProvisioningConfig.builder();
            if(rt.config.hasDefaultUniverse()) {
                builder.setDefaultUniverse(rt.config.getDefaultUniverse());
            }
            for(Map.Entry<String, UniverseSpec> universe : rt.config.getUniverseNamedSpecs().entrySet()) {
                builder.addUniverse(universe.getKey(), universe.getValue());
            }
            for(FeaturePackConfig fpConfig : rt.config.getFeaturePackDeps()) {
                final ChannelSpec channel = fpConfig.getLocation().getChannel();
                if(missingVersions.contains(channel)) {
                    fpConfig = FeaturePackConfig.builder(rt.universeResolver.resolveLatestBuild(fpConfig.getLocation()))
                            .init(fpConfig)
                            .build();
                    missingVersions = CollectionUtils.remove(missingVersions, channel);
                }
                builder.addFeaturePackDep(rt.config.originOf(channel), fpConfig);
            }
            for(ChannelSpec channel : missingVersions) {
                builder.addFeaturePackDep(FeaturePackConfig.forLocation(rt.universeResolver.resolveLatestBuild(channel.getLocation())));
            }
            rt.config = builder.build();
            missingVersions = Collections.emptySet();
            assertVersions();
        }

        if(!conflicts.isEmpty()) {
            throw new ProvisioningDescriptionException(Errors.fpVersionCheckFailed(conflicts.values()));
        }
    }

    private void assertVersions(FeaturePackDepsConfig fpDepsConfig) throws ProvisioningException {
        if(!fpDepsConfig.hasFeaturePackDeps()) {
            return;
        }
        final int branchSize = branch.size();
        final Collection<FeaturePackConfig> fpDeps = fpDepsConfig.getFeaturePackDeps();
        Set<FPID> skip = Collections.emptySet();
        for(FeaturePackConfig fpConfig : fpDeps) {
            final FeaturePackLocation fpl = fpConfig.getLocation();
            if(fpl.getBuild() == null) {
                missingVersions = CollectionUtils.addLinked(missingVersions, fpl.getChannel());
                continue;
            }
            final FeaturePackRuntimeBuilder fp = loaded.get(fpl.getChannel());
            if(fp != null) {
                if(!fp.fpid.equals(fpl.getFPID()) && !branch.contains(fpl.getChannel())) {
                    Set<FPID> versions = conflicts.get(fp.fpid.getChannel());
                    if(versions != null) {
                        versions.add(fpl.getFPID());
                        continue;
                    }
                    versions = new LinkedHashSet<>();
                    versions.add(fp.fpid);
                    versions.add(fpl.getFPID());
                    conflicts = CollectionUtils.putLinked(conflicts, fpl.getChannel(), versions);
                }
                skip = CollectionUtils.add(skip, fp.fpid);
                continue;
            }
            final FeaturePackRuntimeBuilder depFp = rt.getOrLoadFpBuilder(fpConfig.getLocation().getFPID());
            loaded = CollectionUtils.put(loaded, fpConfig.getLocation().getChannel(), depFp);
            if(!missingVersions.isEmpty()) {
                missingVersions = CollectionUtils.remove(missingVersions, fpl.getChannel());
            }
            branch.add(fpl.getChannel());
        }
        for(FeaturePackConfig fpConfig : fpDeps) {
            final FeaturePackLocation fpl = fpConfig.getLocation();
            if(fpl.getBuild() == null || skip.contains(fpl.getFPID())) {
                continue;
            }
            final FeaturePackRuntimeBuilder fp = rt.getFpBuilder(fpl.getChannel(), true);
            assertVersions(fp.spec);
        }
        for(int i = 0; i < branch.size() - branchSize; ++i) {
            branch.remove(branch.size() - 1);
        }
    }
}
