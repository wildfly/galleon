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
package org.jboss.galleon.cli.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.plugin.CliPlugin;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.FeaturePackRuntime;
import org.jboss.galleon.runtime.PackageRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntime.PluginVisitor;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class FeatureContainers {

    public static FeatureContainer fromFeaturePackGav(PmSession session, ProvisioningManager manager, Gav gav,
            String name) throws ProvisioningException, IOException {
        FeatureContainer fp = Caches.getFeaturePackInfo(gav);
        if (fp != null) {
            return fp;
        }
        fp = new FeaturePackInfo(name, gav);
        ProvisioningRuntime rt = buildFullRuntime(gav, manager);
        populateFeatureContainer(fp, session, rt, true);
        Caches.addFeaturePackInfo(gav, fp);
        return fp;
    }

    public static FeatureContainer fromProvisioningRuntime(PmSession session,
            ProvisioningManager manager, ProvisioningRuntime runtime) throws ProvisioningException, IOException {
        ProvisioningInfo info = new ProvisioningInfo();
        populateFeatureContainer(info, session, runtime, false);
        return info;
    }

    private static void populateFeatureContainer(FeatureContainer fp,
            PmSession session, ProvisioningRuntime runtime, boolean allSpecs) throws ProvisioningException, IOException {
        // Need a Map of FeaturePack to resolve external packages/
        Map<String, FeaturePackRuntime> gavs = new HashMap<>();
        for (FeaturePackRuntime rt : runtime.getFeaturePacks()) {
            gavs.put(Identity.buildOrigin(rt.getGav()), rt);
            fp.addDependency(rt.getGav());
        }
        List<CliPlugin> cliPlugins = new ArrayList<>();
        PluginVisitor visitor = new ProvisioningRuntime.PluginVisitor<CliPlugin>() {
            @Override
            public void visitPlugin(CliPlugin plugin) throws ProvisioningException {
                cliPlugins.add(plugin);
            }
        };
        runtime.visitePlugins(visitor, CliPlugin.class);
        CliPlugin plugin = cliPlugins.isEmpty() ? null : cliPlugins.get(0);
        PackageGroupsBuilder pkgBuilder = new PackageGroupsBuilder();
        FeatureSpecsBuilder specsBuilder = new FeatureSpecsBuilder();
        for (FeaturePackRuntime rt : runtime.getFeaturePacks()) {
            pkgBuilder.resetRoots();
            for (PackageRuntime pkg : rt.getPackages()) {
                pkgBuilder.buildGroups(new PackageInfo(pkg, Identity.
                        fromGav(rt.getGav(), pkg.getName()), plugin), new PackageGroupsBuilder.PackageInfoBuilder() {
                    @Override
                    public PackageInfo build(Identity identity) {
                        try {
                            FeaturePackRuntime extRt = gavs.get(identity.getOrigin());
                            if (extRt == null) {
                                throw new RuntimeException("Unknown runtime for " + identity.getOrigin());
                            }
                            PackageRuntime p = extRt.getPackage(identity.getName());
                            if (p == null) {
                                throw new RuntimeException("Package " + pkg.getName()
                                        + ", unknown dependency " + identity + " local is " + rt.getGav());
                            }
                            return new PackageInfo(p, identity, plugin);
                        } catch (IOException | ProvisioningException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, Identity.buildOrigin(rt.getGav()));
            }
            fp.setPackagesRoot(Identity.buildOrigin(rt.getGav()), pkgBuilder.getPackagesRoot());
            // Attach the full set, this targets the container dependency that expose them all.
            if (allSpecs) {
                Group specsRoot = specsBuilder.buildTree(session, rt.getGav(), fp.getGav(), pkgBuilder.getPackages(), allSpecs, null);
                fp.setFeatureSpecRoot(Identity.buildOrigin(rt.getGav()), specsRoot);
            }
        }
        fp.setAllPackages(pkgBuilder.getPackages());

        Map<Gav, Set<ResolvedSpecId>> actualSet = new HashMap<>();
        Map<ResolvedSpecId, List<FeatureInfo>> features = new HashMap<>();
        for (ProvisionedConfig c : runtime.getConfigs()) {
            ConfigInfo config = new ConfigInfo(c.getModel(), c.getName());
            fp.addFinalConfig(config);
            FeatureGroupsBuilder grpBuilder = new FeatureGroupsBuilder();
            c.handle(new ProvisionedConfigHandler() {
                @Override
                public void nextFeature(ProvisionedFeature feature) throws ProvisioningException {
                    Set<ResolvedSpecId> set = actualSet.get(feature.getId().getSpecId().getGav());
                    if (set == null) {
                        set = new HashSet<>();
                        actualSet.put(feature.getId().getSpecId().getGav(), set);
                    }
                    set.add(feature.getId().getSpecId());
                    String fullSpecName = feature.getSpecId().getName();
                    List<String> path = new ArrayList<>();
                    Group parent = grpBuilder.buildFeatureGroups(fullSpecName, feature.getId(), path);
                    FeatureInfo featInfo = new FeatureInfo(config, feature, path, fp.getGav());
                    List<FeatureInfo> lst = features.get(feature.getId().getSpecId());
                    if (lst == null) {
                        lst = new ArrayList<>();
                        features.put(feature.getId().getSpecId(), lst);
                    }
                    lst.add(featInfo);
                    parent.setFeature(featInfo);
                    // Specs have already been computed first place.
                    if (allSpecs) {
                        FeatureSpecInfo spec = specsBuilder.getAllSpecs().get(feature.getSpecId());
                        featInfo.attachSpecInfo(spec);
                        parent.setFeature(featInfo);
                    }
                }
            });
            config.setFeatureGroupRoot(grpBuilder.getRoot());
        }
        if (!allSpecs) {
            // Build the set of FeatureSpecInfo, side effect is to connect
            // packages and feature-specs.
            for (Entry<Gav, Set<ResolvedSpecId>> entry : actualSet.entrySet()) {
                Group specsRoot = specsBuilder.buildTree(session, entry.getKey(), fp.getGav(),
                        pkgBuilder.getPackages(), false, entry.getValue());
                for (ResolvedSpecId rs : entry.getValue()) {
                    List<FeatureInfo> lst = features.get(rs);
                    for (FeatureInfo fi : lst) {
                        fi.attachSpecInfo(specsBuilder.getAllSpecs().get(rs));
                    }
                }
                fp.setFeatureSpecRoot(Identity.buildOrigin(entry.getKey()), specsRoot);
            }
        }
        fp.seAllFeatureSpecs(specsBuilder.getAllSpecs());
        fp.setAllFeatures(features);
    }

    private static ProvisioningRuntime buildFullRuntime(ArtifactCoords.Gav gav, ProvisioningManager manager) throws ProvisioningException {
        FeaturePackConfig config = FeaturePackConfig.forGav(gav);
        ProvisioningConfig provisioning = ProvisioningConfig.builder().addFeaturePackDep(config).build();
        ProvisioningRuntime runtime = manager.getRuntime(provisioning, null, Collections.emptyMap());
        return runtime;
    }
}
