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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.FeatureAnnotation;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.type.FeatureParameterType;
import org.jboss.galleon.type.ParameterTypeConversionException;
import org.jboss.galleon.type.ParameterTypeNotFoundException;
import org.jboss.galleon.type.ParameterTypeProvider;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;


/**
 *
 * @author Alexey Loubyansky
 */
public class ResolvedFeatureSpec extends CapabilityProvider {

    final ResolvedSpecId id;
    final FeatureSpec xmlSpec;
    private Map<String, ResolvedFeatureParam> resolvedParamSpecs = Collections.emptyMap();
    private Map<String, ResolvedFeatureSpec> resolvedRefTargets;
    private Map<ResolvedFeatureId, FeatureDependencySpec> resolvedDeps;

    final boolean parentChildrenBranch;
    final String branchId;
    private final Boolean branchBatch;
    private final Boolean specBranch;

    public ResolvedFeatureSpec(ResolvedSpecId specId, ParameterTypeProvider typeProvider, FeatureSpec spec) throws ProvisioningException {
        this.id = specId;
        this.xmlSpec = spec;

        if(xmlSpec.hasParams()) {
            for(Map.Entry<String, FeatureParameterSpec> entry : xmlSpec.getParams().entrySet()) {
                final FeatureParameterSpec param = entry.getValue();
                resolvedParamSpecs = CollectionUtils.put(resolvedParamSpecs, param.getName(), resolveParamSpec(param, typeProvider));
            }
        }

        final FeatureAnnotation newFb = xmlSpec.getAnnotation(FeatureAnnotation.FEATURE_BRANCH);
        if(newFb != null) {
            branchId = newFb.getElement(FeatureAnnotation.FEATURE_BRANCH_ID);

            if(branchId != null) {
                this.specBranch = true;
            } else {
                Boolean specBranch = null;
                final String elem = newFb.getElement(FeatureAnnotation.FEATURE_BRANCH_SPEC);
                if(elem != null) {
                    specBranch = Boolean.parseBoolean(elem);
                }
                this.specBranch = specBranch;
            }

            final String elem = newFb.getElement(FeatureAnnotation.FEATURE_BRANCH_BATCH);
            if(elem == null) {
                branchBatch = null;
            } else {
                branchBatch = Boolean.parseBoolean(elem);
            }

            parentChildrenBranch = newFb.hasElement(FeatureAnnotation.FEATURE_BRANCH_PARENT_CHILDREN);
        } else {
            branchBatch = null;
            branchId = null;
            parentChildrenBranch = false;
            specBranch = null;
        }
    }

    boolean isSpecBranch(boolean defaultValue) {
        return specBranch == null ? defaultValue : specBranch;
    }

    boolean isBatchBranch(boolean defaultValue) {
        return branchBatch == null ? defaultValue : branchBatch;
    }

    private ResolvedFeatureParam resolveParamSpec(FeatureParameterSpec paramSpec, ParameterTypeProvider typeProvider) throws ProvisioningException {
        final FeatureParameterType type;
        try {
            type = typeProvider.getType(id.producer, paramSpec.getType());
        } catch(ParameterTypeNotFoundException e) {
            throw new ProvisioningException(Errors.failedToResolveParameter(id, paramSpec.getName()), e);
        }
        return new ResolvedFeatureParam(paramSpec, type);
    }

    public ResolvedSpecId getId() {
        return id;
    }

    public String getName() {
        return id.name;
    }

    public FeatureSpec getSpec() {
        return xmlSpec;
    }

    public boolean hasAnnotations() {
        return xmlSpec.hasAnnotations();
    }

    public Collection<FeatureAnnotation> getAnnotations() {
        return xmlSpec.getAnnotations();
    }

    public boolean hasParams() {
        return !resolvedParamSpecs.isEmpty();
    }

    public Set<String> getParamNames() {
        return resolvedParamSpecs.keySet();
    }

    Map<String, ResolvedFeatureParam> getResolvedParams() {
        return resolvedParamSpecs;
    }

    ResolvedFeatureParam getResolvedParam(String name) throws ProvisioningDescriptionException {
        final ResolvedFeatureParam p = resolvedParamSpecs.get(name);
        if(p == null) {
            throw new ProvisioningDescriptionException(Errors.unknownFeatureParameter(id, name));
        }
        return p;
    }

    Map<String, Object> resolveNonIdParams(ResolvedFeatureId parentId, String parentRef, Map<String, String> params) throws ProvisioningException {
        Map<String, Object> resolvedParams = Collections.emptyMap();
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                if(xmlSpec.getParam(param.getKey()).isFeatureId()) {
                    continue;
                }
                resolvedParams = CollectionUtils.put(resolvedParams, param.getKey(), paramFromString(param.getKey(), param.getValue()));
            }
        }

        if(parentId == null) {
            return resolvedParams;
        }

        if(parentRef == null) {
            parentRef = parentId.specId.name;
        }

        final FeatureReferenceSpec refSpec = xmlSpec.getFeatureRef(parentRef);
        if (refSpec.hasMappedParams()) {
            for (Map.Entry<String, String> mapping : refSpec.getMappedParams().entrySet()) {
                if (xmlSpec.getParam(mapping.getKey()).isFeatureId()) {
                    continue;
                }
                final Object idValue = parentId.params.get(mapping.getValue());
                if (idValue != null) {
                    resolvedParams = CollectionUtils.put(resolvedParams, mapping.getKey(), idValue);
                }
            }
        } else {
            for (Map.Entry<String, Object> parentEntry : parentId.params.entrySet()) {
                if (xmlSpec.getParam(parentEntry.getKey()).isFeatureId()) {
                    continue;
                }
                resolvedParams = CollectionUtils.put(resolvedParams, parentEntry.getKey(), parentEntry.getValue());
            }
        }

        return resolvedParams;
    }

    private Object paramFromString(String name, String value) throws ProvisioningException {
        try {
            return getResolvedParam(name).type.fromString(value);
        } catch (ParameterTypeConversionException e) {
            throw new ProvisioningException(Errors.failedToResolveParameter(id, name, value), e);
        }
    }

    String paramToString(String name, Object value) throws ProvisioningException {
        return getResolvedParam(name).type.toString(value);
    }

    boolean resolveCapabilityElement(ResolvedFeature feature, String paramName, CapabilityResolver capResolver) throws ProvisioningException {
        final ResolvedFeatureParam resolvedParam = getResolvedParam(paramName);
        Object value = feature.getResolvedParam(paramName);
        if(value == null) {
            value = feature.isUnset(paramName) ? null : resolvedParam.defaultValue;
            if (value == null) {
                if (capResolver.getSpec().isOptional()) {
                    return false;
                }
                throw new ProvisioningException(Errors.capabilityMissingParameter(capResolver.getSpec(), paramName));
            }
        }
        if(Constants.GLN_UNDEFINED.equals(value)) {
            return true; // skip GLN_UNDEFINED
        }
        return resolvedParam.type.resolveCapabilityElement(capResolver, value);
    }

    FeatureParameterType getTypeForParameter(String paramName) throws ParameterTypeNotFoundException, ProvisioningDescriptionException {
        return getResolvedParam(paramName).type;
    }

    ResolvedFeatureId resolveIdFromForeignKey(ResolvedFeatureId parentId, String parentRef, Map<String, String> params) throws ProvisioningException {
        if(!xmlSpec.hasId()) {
            return null;
        }
        if(parentId == null) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Failed to initialize foreign key parameters of ").append(id).append(": the referenced feature has not ID ");
            throw new ProvisioningException(buf.toString());
        }
        if(parentRef == null) {
            parentRef = parentId.specId.name;
        }

        final List<FeatureParameterSpec> idParamSpecs = xmlSpec.getIdParams();
        final Map<String, Object> resolvedParams = new HashMap<>(idParamSpecs.size());
        final FeatureReferenceSpec refSpec = xmlSpec.getFeatureRef(parentRef);

        try {
            if (refSpec.hasMappedParams()) {
                for (Map.Entry<String, String> mapping : refSpec.getMappedParams().entrySet()) {
                    final FeatureParameterSpec param = xmlSpec.getParam(mapping.getKey());
                    if(!param.isFeatureId()) {
                        continue;
                    }
                    final Object idValue = parentId.params.get(mapping.getValue());
                    if (idValue == null) {
                        throw new ProvisioningDescriptionException(id + " expects ID parameter '" + mapping.getValue() + "' in " + parentId);
                    }
                    resolvedParams.put(mapping.getKey(), idValue);
                }
                for(FeatureParameterSpec idParamSpec : idParamSpecs) {
                    String configValue = params.get(idParamSpec.getName());
                    if(configValue != null) {
                        final Object childValue = paramFromString(idParamSpec.getName(), configValue);
                        final Object idValue = resolvedParams.put(idParamSpec.getName(), childValue);
                        if(idValue != null && !idValue.equals(childValue)) {
                            throw new ProvisioningDescriptionException(Errors.idParamForeignKeyInitConflict(id, idParamSpec.getName(), childValue, idValue));
                        }
                        continue;
                    }

                    if(resolvedParams.containsKey(idParamSpec.getName())) {
                        continue;
                    }

                    final Object childValue = getResolvedParam(idParamSpec.getName()).defaultValue;
                    if(childValue == null) {
                        throw new ProvisioningDescriptionException(Errors.nonNillableParameterIsNull(id, idParamSpec.getName()));
                    }
                    resolvedParams.put(idParamSpec.getName(), childValue);
                }
            } else {
                for (FeatureParameterSpec idParamSpec : idParamSpecs) {
                    final Object parentValue = parentId.params.get(idParamSpec.getName());
                    String configValue = params.get(idParamSpec.getName());
                    if(configValue != null) {
                        final Object childValue = paramFromString(idParamSpec.getName(), configValue);
                        if(parentValue != null && !parentValue.equals(childValue)) {
                            throw new ProvisioningDescriptionException(Errors.idParamForeignKeyInitConflict(id, idParamSpec.getName(), childValue, parentValue));
                        }
                        resolvedParams.put(idParamSpec.getName(), childValue);
                        continue;
                    }

                    if(parentValue != null) {
                        resolvedParams.put(idParamSpec.getName(), parentValue);
                        continue;
                    }

                    final Object childValue = getResolvedParam(idParamSpec.getName()).defaultValue;
                    if(childValue == null) {
                        throw new ProvisioningDescriptionException(Errors.nonNillableParameterIsNull(id, idParamSpec.getName()));
                    }
                    resolvedParams.put(idParamSpec.getName(), childValue);
                }
            }

            return new ResolvedFeatureId(id, resolvedParams);
        } catch(ProvisioningException e) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Failed to initialize foreign key parameters of ").append(id).append(" spec referencing feature ").append(parentId).append(" with parameters ");
            StringUtils.append(buf, params.entrySet());
            throw new ProvisioningException(Errors.failedToInitializeForeignKeyParams(id, parentId, params), e);
        }
    }

    ResolvedFeatureId resolveFeatureId(Map<String, String> params) throws ProvisioningException {
        if(!xmlSpec.hasId()) {
            return null;
        }
        final List<FeatureParameterSpec> idSpecs = xmlSpec.getIdParams();
        if(idSpecs.size() == 1) {
            final FeatureParameterSpec idSpec = idSpecs.get(0);
            return new ResolvedFeatureId(id, Collections.singletonMap(idSpec.getName(), resolveIdParamValue(params, idSpec)));
        }
        final Map<String, Object> resolvedParams = new HashMap<>(idSpecs.size());
        for(FeatureParameterSpec param : idSpecs) {
            resolvedParams.put(param.getName(), resolveIdParamValue(params, param));
        }
        return new ResolvedFeatureId(id, resolvedParams);
    }

    private Object resolveIdParamValue(Map<String, String> params, final FeatureParameterSpec param) throws ProvisioningException {
        final ResolvedFeatureParam resolvedParam = getResolvedParam(param.getName());
        final String strValue = params.get(param.getName());
        if(strValue == null) {
            final Object value = resolvedParam.defaultValue;
            if (value == null) {
                throw new ProvisioningDescriptionException(Errors.nonNillableParameterIsNull(id, param.getName()));
            }
            return value;
        }
        return resolvedParam.type.fromString(strValue);
    }

    private Map<ResolvedFeatureId, FeatureDependencySpec> resolveSpecDeps(ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        if(resolvedDeps != null) {
            return resolvedDeps;
        }
        resolvedDeps = Collections.emptyMap();
        if(xmlSpec.hasFeatureDeps()) {
            resolvedDeps = resolveFeatureDeps(rt, xmlSpec.getFeatureDeps());
        }
        return resolvedDeps;
    }

    Map<ResolvedFeatureId, FeatureDependencySpec> resolveFeatureDeps(ProvisioningRuntimeBuilder rt, final Collection<FeatureDependencySpec> depSpecs) throws ProvisioningException {
        if(depSpecs.isEmpty()) {
            return resolveSpecDeps(rt);
        }
        final Map<ResolvedFeatureId, FeatureDependencySpec> resolvedSpecDeps = resolveSpecDeps(rt);
        final Map<ResolvedFeatureId, FeatureDependencySpec> result;
        if(resolvedSpecDeps.isEmpty()) {
            if(depSpecs.size() == 1) {
                final FeatureDependencySpec depSpec = depSpecs.iterator().next();
                final FeaturePackRuntimeBuilder depFp = depSpec.getOrigin() == null ? rt.layout.getFeaturePack(id.producer) : rt.getOrigin(depSpec.getOrigin());
                final ResolvedFeatureSpec depResolvedSpec = rt.getFeatureSpec(depFp, depSpec.getFeatureId().getSpec().getName());
                return Collections.singletonMap(depResolvedSpec.resolveFeatureId(depSpec.getFeatureId().getParams()), depSpec);
            }
            result = new LinkedHashMap<>(depSpecs.size());
        } else {
            result = new LinkedHashMap<>(resolvedSpecDeps.size() + depSpecs.size());
            result.putAll(resolvedSpecDeps);
        }
        final FeaturePackRuntimeBuilder ownFp = rt.layout.getFeaturePack(id.producer);
        for (FeatureDependencySpec userDep : depSpecs) {
            final FeaturePackRuntimeBuilder depFp = userDep.getOrigin() == null ? ownFp : rt.getOrigin(userDep.getOrigin());
            final ResolvedFeatureSpec depResolvedSpec = rt.getFeatureSpec(depFp, userDep.getFeatureId().getSpec().getName());
            final ResolvedFeatureId depId = depResolvedSpec.resolveFeatureId(userDep.getFeatureId().getParams());
            final FeatureDependencySpec specDep = result.put(depId, userDep);
            if(specDep != null) {
                if(!userDep.isInclude() && specDep.isInclude()) {
                    result.put(depId, specDep);
                }
            }
        }
        return result;
    }

    void resolveRefMappings(ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        if(!xmlSpec.hasFeatureRefs()) {
            resolvedRefTargets = Collections.emptyMap();
            return;
        }
        final FeaturePackRuntimeBuilder ownFp = rt.layout.getFeaturePack(id.producer);

        Collection<FeatureReferenceSpec> refs = xmlSpec.getFeatureRefs();
        if (refs.size() == 1) {
            resolvedRefTargets = Collections.singletonMap(refs.iterator().next().getName(), resolveRefMapping(rt, ownFp, refs.iterator().next()));
            return;
        }

        final Map<String, ResolvedFeatureSpec> tmp = new HashMap<>(refs.size());
        for (FeatureReferenceSpec refSpec : refs) {
            tmp.put(refSpec.getName(), resolveRefMapping(rt, ownFp, refSpec));
        }
        this.resolvedRefTargets = Collections.unmodifiableMap(tmp);
    }

    private ResolvedFeatureSpec resolveRefMapping(ProvisioningRuntimeBuilder rt, FeaturePackRuntimeBuilder origin,
            FeatureReferenceSpec refSpec) throws ProvisioningException {
        try {
            if(refSpec.getOrigin() != null) {
                origin = rt.layout.getFeaturePack(origin.spec.getFeaturePackDep(refSpec.getOrigin()).getLocation().getProducer());
            }
            final ResolvedFeatureSpec resolvedRefSpec = rt.getFeatureSpec(origin, refSpec.getFeature().getName());
            assertRefParamMapping(refSpec, resolvedRefSpec);
            return resolvedRefSpec;
        } catch (ProvisioningDescriptionException e) {
            throw new ProvisioningDescriptionException(Errors.failedToResolveFeatureReference(refSpec, id), e);
        }
    }

    private void assertRefParamMapping(final FeatureReferenceSpec refSpec, final ResolvedFeatureSpec targetSpec)
            throws ProvisioningDescriptionException {
        if (!targetSpec.xmlSpec.hasId()) {
            throw new ProvisioningDescriptionException(id + " feature spec declares reference "
                    + refSpec.getName() + " to feature spec " + targetSpec.id
                    + " that has no ID parameters");
        }
        if(!refSpec.hasMappedParams()) {
            for(FeatureParameterSpec targetIdParam : targetSpec.xmlSpec.getIdParams()) {
                if(!xmlSpec.hasParam(targetIdParam.getName())) {
                    throw new ProvisioningDescriptionException(Errors.nonExistingForeignKeyParam(refSpec.getName(), id, targetIdParam.getName()));
                }
            }
            return;
        }
        if (targetSpec.xmlSpec.getIdParams().size() != refSpec.getParamsMapped()) {
            throw new ProvisioningDescriptionException("The number of foreign key parameters of reference " + refSpec.getName() +
                    " in feature spec " + id + " does not match the number of the ID parameters of the referenced feature spec "
                    + targetSpec.id);
        }
        for(Map.Entry<String, String> mapping : refSpec.getMappedParams().entrySet()) {
            if (!xmlSpec.hasParam(mapping.getKey())) {
                throw new ProvisioningDescriptionException(Errors.nonExistingForeignKeyParam(refSpec.getName(), id, mapping.getKey()));
            }
            if (!targetSpec.xmlSpec.hasParam(mapping.getValue())) {
                throw new ProvisioningDescriptionException(
                        Errors.nonExistingForeignKeyTarget(mapping.getKey(), refSpec.getName(), id, mapping.getValue(), targetSpec.id));
            }
        }
    }

    List<ResolvedFeatureId> resolveRefs(ResolvedFeature feature) throws ProvisioningException {
        if(resolvedRefTargets.isEmpty()) {
            return Collections.emptyList();
        }
        List<ResolvedFeatureId> refIds = new ArrayList<>(resolvedRefTargets.size());
        for(Map.Entry<String, ResolvedFeatureSpec> refEntry : resolvedRefTargets.entrySet()) {
            final List<ResolvedFeatureId> resolvedIds = resolveRefId(feature, xmlSpec.getFeatureRef(refEntry.getKey()), refEntry.getValue(), false);
            if(!resolvedIds.isEmpty()) {
                refIds = CollectionUtils.addAll(refIds, resolvedIds);
            }
        }
        return refIds;
    }

    List<ResolvedFeatureId> resolveRefId(final ResolvedFeature feature, final FeatureReferenceSpec refSpec, final ResolvedFeatureSpec targetSpec)
            throws ProvisioningException {
        return resolveRefId(feature, refSpec, targetSpec, true);
    }

    private List<ResolvedFeatureId> resolveRefId(final ResolvedFeature feature, final FeatureReferenceSpec refSpec, final ResolvedFeatureSpec targetSpec, boolean assertRefMapping)
            throws ProvisioningException {

        if(assertRefMapping) {
            assertRefParamMapping(refSpec, targetSpec);
        }

        ArrayList<Map<String,Object>> paramsList = null;
        Map<String, Object> params = Collections.emptyMap();
        boolean child = feature.hasId() ? false : true; // no id is considered a child to make the list-add not break the branch
        if(refSpec.hasMappedParams()) {
            for (Map.Entry<String, String> mapping : refSpec.getMappedParams().entrySet()) {
                final String paramName = mapping.getKey();
                final String refParamName = mapping.getValue();

                final ResolvedFeatureParam resolvedParam = resolvedParamSpecs.get(paramName);
                if(!child && (resolvedParam.spec.isFeatureId() && targetSpec.getSpec().getParam(refParamName).isFeatureId())) {
                    child = true;
                }
                Object paramValue = feature.getResolvedParam(paramName);
                if (paramValue == null) {
                    paramValue = feature.isUnset(paramName) ? null : resolvedParam.defaultValue;
                    if (paramValue == null) {
                        assertRefNotNillable(feature, refSpec);
                        return Collections.emptyList();
                    }
                }
                if(paramValue.equals(Constants.GLN_UNDEFINED)) {
                    continue;
                }
                if(resolvedParam.type.isCollection()) {
                    final Collection<?> col = (Collection<?>) paramValue;
                    if(col.isEmpty()) {
                        assertRefNotNillable(feature, refSpec);
                        return Collections.emptyList();
                    }
                    if(paramsList == null) {
                        paramsList = new ArrayList<>(col.size());
                        paramsList.add(params);
                    } else {
                        paramsList.ensureCapacity(paramsList.size()*col.size());
                    }
                    final int listSize = paramsList.size();
                    for(int i = 0; i < listSize; ++i) {
                        final Map<String, Object> idParams = paramsList.get(i);
                        int colI = 0;
                        for(Object item : col) {
                            if(item.equals(Constants.GLN_UNDEFINED)) {
                                continue;
                            }
                            if(colI++ == 0) {
                                final Map<String, Object> clone = col.size() == 1 ? idParams : CollectionUtils.clone(idParams);
                                paramsList.set(i, CollectionUtils.put(clone, refParamName, item));
                                continue;
                            }
                            paramsList.add(CollectionUtils.put(CollectionUtils.clone(idParams), refParamName, item));
                        }
                    }
                    continue;
                }
                if (paramsList != null) {
                    for(int i = 0; i < paramsList.size(); ++i) {
                        paramsList.set(i, CollectionUtils.put(paramsList.get(i), refParamName, paramValue));
                    }
                    continue;
                }
                params = CollectionUtils.put(params, refParamName, paramValue);
            }
        } else {
            for(FeatureParameterSpec targetIdParam : targetSpec.xmlSpec.getIdParams()) {
                final String paramName = targetIdParam.getName();
                final String refParamName = paramName;

                final ResolvedFeatureParam resolvedParam = resolvedParamSpecs.get(paramName);
                if(!child && resolvedParam.spec.isFeatureId()) {
                    child = true;
                }
                Object paramValue = feature.getResolvedParam(paramName);
                if (paramValue == null) {
                    paramValue = feature.isUnset(paramName) ? null : resolvedParam.defaultValue;
                    if (paramValue == null) {
                        assertRefNotNillable(feature, refSpec);
                        return Collections.emptyList();
                    }
                }
                if(paramValue.equals(Constants.GLN_UNDEFINED)) {
                    continue;
                }

                if(resolvedParam.type.isCollection()) {
                    final Collection<?> col = (Collection<?>) paramValue;
                    if(col.isEmpty()) {
                        assertRefNotNillable(feature, refSpec);
                        return Collections.emptyList();
                    }
                    if(paramsList == null) {
                        paramsList = new ArrayList<>(col.size());
                        paramsList.add(params);
                    } else {
                        paramsList.ensureCapacity(paramsList.size()*col.size());
                    }
                    final int listSize = paramsList.size();
                    for(int i = 0; i < listSize; ++i) {
                        final Map<String, Object> idParams = paramsList.get(i);
                        int colI = 0;
                        for(Object item : col) {
                            if(item.equals(Constants.GLN_UNDEFINED)) {
                                continue;
                            }
                            if(colI++ == 0) {
                                final Map<String, Object> clone = col.size() == 1 ? idParams : CollectionUtils.clone(idParams);
                                paramsList.set(i, CollectionUtils.put(clone, refParamName, item));
                                continue;
                            }
                            paramsList.add(CollectionUtils.put(CollectionUtils.clone(idParams), refParamName, item));
                        }
                    }
                    continue;
                }
                if (paramsList != null) {
                    for(int i = 0; i < paramsList.size(); ++i) {
                        paramsList.set(i, CollectionUtils.put(paramsList.get(i), refParamName, paramValue));
                    }
                    continue;
                }
                params = CollectionUtils.put(params, refParamName, paramValue);
            }
        }

        if(paramsList != null) {
            final List<ResolvedFeatureId> refIds = new ArrayList<>(paramsList.size());
            for(int i = 0; i < paramsList.size(); ++i) {
                final Map<String, Object> idParams = paramsList.get(i);
                if(idParams.isEmpty()) {
                    // TODO
                    continue;
                }
                refIds.add(new ResolvedFeatureId(targetSpec.id, idParams, child));
            }
            if(refIds.isEmpty()) {
                assertRefNotNillable(feature, refSpec);
            }
            return refIds;
        }
        if(params.isEmpty()) {
            assertRefNotNillable(feature, refSpec);
            return Collections.emptyList();
        }
        return Collections.singletonList(new ResolvedFeatureId(targetSpec.id, params, child));
    }

    private void assertRefNotNillable(final ResolvedFeature feature, final FeatureReferenceSpec refSpec)
            throws ProvisioningDescriptionException {
        if (!refSpec.isNillable()) {
            throw new ProvisioningDescriptionException(Errors.nonNillableRefIsNull(feature, refSpec.getName()));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((resolvedDeps == null) ? 0 : resolvedDeps.hashCode());
        result = prime * result + ((resolvedParamSpecs == null) ? 0 : resolvedParamSpecs.hashCode());
        result = prime * result + ((resolvedRefTargets == null) ? 0 : resolvedRefTargets.hashCode());
        result = prime * result + ((xmlSpec == null) ? 0 : xmlSpec.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResolvedFeatureSpec other = (ResolvedFeatureSpec) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (resolvedDeps == null) {
            if (other.resolvedDeps != null)
                return false;
        } else if (!resolvedDeps.equals(other.resolvedDeps))
            return false;
        if (resolvedParamSpecs == null) {
            if (other.resolvedParamSpecs != null)
                return false;
        } else if (!resolvedParamSpecs.equals(other.resolvedParamSpecs))
            return false;
        if (resolvedRefTargets == null) {
            if (other.resolvedRefTargets != null)
                return false;
        } else if (!resolvedRefTargets.equals(other.resolvedRefTargets))
            return false;
        if (xmlSpec == null) {
            if (other.xmlSpec != null)
                return false;
        } else if (!xmlSpec.equals(other.xmlSpec))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResolvedFeatureSpec{" + "id=" + id + ", xmlSpec=" + xmlSpec + ", resolvedRefTargets=" + resolvedRefTargets + ", resolvedDeps=" + resolvedDeps + '}';
    }

}
