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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.galleon.config.ProvisioningConfig;

import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class FeatureContainer {

    public static final String ROOT = "###ROOT";

    private final Map<String, List<ConfigInfo>> finalConfigs = new HashMap<>();
    private final Map<String, Group> packagesRoots = new HashMap<>();
    private final Map<String, Group> featuresSpecRoots = new HashMap<>();
    private Map<String, FeatureContainer> fullDependencies = new HashMap<>();
    private final String name;
    private final FPID fpid;
    private boolean edit;
    private Map<ResolvedSpecId, FeatureSpecInfo> allSpecs;
    private Map<Identity, Group> allPackages;
    private Map<ResolvedSpecId, List<FeatureInfo>> allFeatures;
    private final ProvisioningConfig config;

    protected FeatureContainer(String name, FPID fpid, ProvisioningConfig config) {
        this.name = name;
        this.fpid = fpid;
        this.config = config;
    }

    public ProvisioningConfig getProvisioningConfig() {
        return config;
    }

    public FPID getFPID() {
        return fpid;
    }

    public String getName() {
        return name;
    }

    public void setFullDependencies(Map<String, FeatureContainer> fullDependencies) {
        this.fullDependencies = Collections.unmodifiableMap(fullDependencies);
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isEdit() {
        return edit;
    }

    public Map<String, FeatureContainer> getFullDependencies() {
        return fullDependencies;
    }

    protected void addFinalConfig(ConfigInfo info) {
        List<ConfigInfo> lst = finalConfigs.get(info.getModel());
        if (lst == null) {
            lst = new ArrayList<>();
            finalConfigs.put(info.getModel(), lst);
        }
        lst.add(info);
    }

    public Map<String, List<ConfigInfo>> getFinalConfigs() {
        return Collections.unmodifiableMap(finalConfigs);
    }

    protected void setFeatureSpecRoot(String origin, Group featuresSpecRoot) {
        featuresSpecRoots.put(origin, featuresSpecRoot);
    }

    protected void setPackagesRoot(String origin, Group packagesRoot) {
        packagesRoots.put(origin, packagesRoot);
    }

    public Map<String, Group> getFeatureSpecs() {
        return featuresSpecRoots;
    }

    public Map<String, Group> getPackages() {
        return packagesRoots;
    }

    public Map<ResolvedSpecId, FeatureSpecInfo> getAllSpecs() {
        return Collections.unmodifiableMap(allSpecs);
    }

    void seAllFeatureSpecs(Map<ResolvedSpecId, FeatureSpecInfo> allSpecs) {
        this.allSpecs = allSpecs;
    }
    public Map<Identity, Group> getAllPackages() {
        return Collections.unmodifiableMap(allPackages);
    }

    void setAllPackages(Map<Identity, Group> allPackages) {
        this.allPackages = allPackages;
    }

    public Map<ResolvedSpecId, List<FeatureInfo>> getAllFeatures() {
        return Collections.unmodifiableMap(allFeatures);
    }

    void setAllFeatures(Map<ResolvedSpecId, List<FeatureInfo>> allFeatures) {
        this.allFeatures = allFeatures;
    }
}
