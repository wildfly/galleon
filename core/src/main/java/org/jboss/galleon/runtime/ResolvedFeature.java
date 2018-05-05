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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.type.FeatureParameterType;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ResolvedFeature extends CapabilityProvider implements ProvisionedFeature {

    /*
     * These states are used when the features are being ordered in the config
     */
    private static final byte FREE = 0;
    private static final byte SCHEDULED = 1;
    private static final byte ORDERED = 2;

    private static final byte START = 1;
    private static final byte END = 2;

    final int includeNo;
    final ResolvedFeatureId id;
    final ResolvedFeatureSpec spec;
    Map<String, Object> params;
    Set<String> resetParams = Collections.emptySet();
    Set<String> unsetParams = Collections.emptySet();
    Map<ResolvedFeatureId, FeatureDependencySpec> deps;

    private byte orderingState = FREE;
    private byte batchControl;
    private boolean branchStart;
    private boolean branchEnd;

    private SpecFeatures specFeatures;
    ConfigFeatureBranch branch;
    List<ResolvedFeature> branchDependees;
    Map<ConfigFeatureBranch, Boolean> branchDeps = new HashMap<>();

    ResolvedFeature(ResolvedFeatureId id, ResolvedFeatureSpec spec, int includeNo) {
        this.includeNo = includeNo;
        this.id = id;
        this.spec = spec;
        params = id == null ? new HashMap<>() : new HashMap<>(id.params);
    }

    ResolvedFeature(ResolvedFeatureId id, ResolvedFeatureSpec spec, Map<String, Object> params, Map<ResolvedFeatureId, FeatureDependencySpec> resolvedDeps, int includeNo)
            throws ProvisioningException {
        this.includeNo = includeNo;
        this.id = id;
        this.spec = spec;
        this.deps = resolvedDeps;
        this.params = id == null ? new HashMap<>() : new HashMap<>(id.params);
        if (!params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                setParam(entry.getKey(), entry.getValue(), true);
            }
        }
    }

    ResolvedFeature copy(int includeNo) throws ProvisioningException {
        final ResolvedFeature copy = new ResolvedFeature(id, spec, params.size() > 1 ? new HashMap<>(params) : params, deps.size() > 1 ? new LinkedHashMap<>(deps) : deps, includeNo);
        if(!resetParams.isEmpty()) {
            copy.resetParams = CollectionUtils.clone(resetParams);
        }
        if(!unsetParams.isEmpty()) {
            copy.unsetParams = CollectionUtils.clone(unsetParams);
        }
        return copy;
    }

    void validate() throws ProvisioningDescriptionException {
        for(Map.Entry<String, ResolvedFeatureParam> entry : spec.getResolvedParams().entrySet()) {
            final ResolvedFeatureParam param = entry.getValue();
            if(params.containsKey(entry.getKey())) {
                continue;
            }
            if(param.defaultValue == null || unsetParams.contains(entry.getKey())) {
                if(param.spec.isNillable()) {
                    continue;
                }
                throw new ProvisioningDescriptionException(Errors.nonNillableParameterIsNull(this, entry.getKey()));
            }
            params.put(entry.getKey(), param.defaultValue);
        }
    }

    boolean isFree() {
        return orderingState == FREE;
    }

    boolean isOrdered() {
        return orderingState == ORDERED;
    }

    void schedule() {
        if(orderingState != FREE) {
            throw new IllegalStateException();
        }
        orderingState = SCHEDULED;
    }

    void ordered() throws ProvisioningDescriptionException {
        validate(); // may not be the best place for this
        if(orderingState != SCHEDULED) {
            throw new IllegalStateException();
        }
        orderingState = ORDERED;
        provided(branch);
        spec.provided(branch);
    }

    void free() {
        orderingState = FREE;
        branchDeps.clear();
    }

    void addBranchDep(ConfigFeatureBranch branchDep, boolean child) {
        final Boolean prevChild = branchDeps.get(branchDep);
        if(prevChild == null || !prevChild && child) {
            if(branchDeps.put(branchDep, child) != null && branch != null) {
                branch.addBranchDep(branchDep);
            }
        }
    }

    void addBranchDependee(ResolvedFeature feature) {
        if(branchDependees == null) {
            branchDependees = new ArrayList<>();
        }
        branchDependees.add(feature);
    }

    void setBranch(ConfigFeatureBranch branch) throws ProvisioningException {
        this.branch = branch;
        if(branchDeps.size() > 1 || !branchDeps.containsKey(branch)) {
            final Iterator<ConfigFeatureBranch> iter = branchDeps.keySet().iterator();
            while(iter.hasNext()) {
                final ConfigFeatureBranch branchDep = iter.next();
                if(!branch.id.equals(branchDep.id)) {
                    branch.addBranchDep(branchDep);
                }
            }
        }
        if(branchDependees != null) {
            for(ResolvedFeature branchDependee : branchDependees) {
                branchDependee.addBranchDep(branch, false);
            }
            branchDependees = null;
        }
        ordered();
    }

    void startBatch() {
        batchControl = START;
    }

    void endBatch() {
        batchControl = batchControl == START ? 0 : END;
    }

    boolean isBatchStart() {
        return batchControl == START;
    }

    void clearBatchStart() {
        batchControl = 0;
    }

    boolean isBatchEnd() {
        return batchControl == END;
    }

    void clearBatchEnd() {
        batchControl = 0;
    }

    void startBranch() {
        branchStart = true;
    }

    void endBranch() {
        branchEnd = true;
    }

    boolean isBranchStart() {
        return branchStart;
    }

    boolean isBranchEnd() {
        return branchEnd;
    }

    public void addDependency(ResolvedFeatureId id, FeatureDependencySpec depSpec) throws ProvisioningDescriptionException {
        if(deps.containsKey(id)) {
            throw new ProvisioningDescriptionException("Duplicate dependency on " + id + " from " + this.id); // TODO
        }
        deps = CollectionUtils.putLinked(deps, id, depSpec);
    }

    @Override
    public boolean hasId() {
        return id != null;
    }

    @Override
    public ResolvedFeatureId getId() {
        return id;
    }

    @Override
    public ResolvedSpecId getSpecId() {
        return spec.id;
    }

    @Override
    public boolean hasParams() {
        return !params.isEmpty();
    }

    @Override
    public Collection<String> getParamNames() {
        return params.keySet();
    }

    @Override
    public Object getResolvedParam(String name) {
        return params.get(name);
    }

    @Override
    public String getConfigParam(String name) throws ProvisioningException {
        return spec.paramToString(name, params.get(name));
    }

    @Override
    public Map<String, Object> getResolvedParams() {
        return params;
    }

    void setParam(String name, Object value, boolean overwrite) throws ProvisioningException {
        if(id != null) {
            final Object idValue = id.params.get(name);
            if(idValue != null) {
                if(!idValue.equals(value)) {
                    throw new ProvisioningDescriptionException("ID parameter " + name + "=" + idValue + " can't be reset to " + value);
                }
                return;
            }
        }
        if(!spec.xmlSpec.hasParam(name)) {
            throw new ProvisioningDescriptionException(Errors.unknownFeatureParameter(spec.id, name));
        }

        if(unsetParams.contains(name)) {
            if(!overwrite) {
                return;
            }
            unsetParams = CollectionUtils.remove(unsetParams, name);
            params.put(name, value);
            return;
        }

        if(resetParams.contains(name)) {
            if(!overwrite) {
                return;
            }
            resetParams = CollectionUtils.remove(resetParams, name);
            params.put(name, value);
            return;
        }

        final Object prevValue = params.get(name);
        if(prevValue == null) {
            params.put(name, value);
            return;
        }
        final FeatureParameterType valueType = spec.getTypeForParameter(name);
        if(valueType.isMergeable()) {
            params.put(name, overwrite ? valueType.merge(prevValue, value) : valueType.merge(value, prevValue));
            return;
        }
        if(overwrite) {
            params.put(name, value);
        }
    }

    boolean isUnset(String name) {
        return unsetParams.contains(name);
    }

    void unsetParam(String name, boolean overwrite) throws ProvisioningDescriptionException {
        if(!spec.xmlSpec.hasParam(name)) {
            throw new ProvisioningDescriptionException(Errors.unknownFeatureParameter(spec.id, name));
        }
        if(id.params.containsKey(name)) {
            throw new ProvisioningDescriptionException(Errors.featureIdParameterCantBeUnset(id, name));
        }
        if(unsetParams.contains(name)) {
            return;
        }
        if (resetParams.contains(name)) {
            if(!overwrite) {
                return;
            }
            resetParams = CollectionUtils.remove(resetParams, name);
        } else if (overwrite) {
            params.remove(name);
        } else if (params.containsKey(name)) {
            return;
        }
        unsetParams = CollectionUtils.add(unsetParams, name);
    }

    void unsetAllParams(Set<String> names, boolean overwrite) throws ProvisioningDescriptionException {
        if(names.isEmpty()) {
            return;
        }
        for(String name : names) {
            unsetParam(name, overwrite);
        }
    }

    void resetParam(String name) throws ProvisioningDescriptionException {
        if(!spec.xmlSpec.hasParam(name)) {
            throw new ProvisioningDescriptionException(Errors.unknownFeatureParameter(spec.id, name));
        }
        if(id.params.containsKey(name)) {
            throw new ProvisioningDescriptionException(Errors.featureIdParameterCantBeReset(id, name));
        }
        if(resetParams.contains(name)) {
            return;
        }
        if(unsetParams.contains(name)) {
            unsetParams = CollectionUtils.remove(unsetParams, name);
        } else {
            params.remove(name);
        }
        resetParams = CollectionUtils.add(resetParams, name);
    }

    void resetAllParams(Set<String> names) throws ProvisioningDescriptionException {
        if(names.isEmpty()) {
            return;
        }
        for(String name : names) {
            resetParam(name);
        }
    }

    void merge(ResolvedFeature other, boolean overwriteParams) throws ProvisioningException {
        merge(other.deps, other.getResolvedParams(), overwriteParams);
        if(!other.unsetParams.isEmpty()) {
            unsetAllParams(other.unsetParams, overwriteParams);
        }
        if(overwriteParams) {
            if(!other.resetParams.isEmpty()) {
                resetAllParams(other.resetParams);
            }
        }
    }

    void merge(Map<ResolvedFeatureId, FeatureDependencySpec> deps, Map<String, Object> resolvedParams, boolean overwriteParams) throws ProvisioningException {
        if(!resolvedParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : resolvedParams.entrySet()) {
                setParam(entry.getKey(), entry.getValue(), overwriteParams);
            }
        }
        if(!deps.isEmpty()) {
            for(Map.Entry<ResolvedFeatureId, FeatureDependencySpec> dep : deps.entrySet()) {
                addDependency(dep.getKey(), dep.getValue());
            }
        }
    }

    List<ResolvedFeatureId> resolveRefs() throws ProvisioningException {
        return spec.resolveRefs(this);
    }

    void setSpecFeatures(SpecFeatures specFeatures) {
        this.specFeatures = specFeatures;
    }

    SpecFeatures getSpecFeatures() {
        return specFeatures;
    }

    @Override
    public String toString() {
        return "ResolvedFeature{" + "includeNo=" + includeNo + ", id=" + id + ", spec=" + spec + ", params=" + params + ", deps=" + deps + ", orderingState=" + orderingState + ", batchControl=" + batchControl + '}';
    }
}