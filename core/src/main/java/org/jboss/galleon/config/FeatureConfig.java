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
package org.jboss.galleon.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureConfig implements ConfigItem, ConfigItemContainer, ConfigItemContainerBuilder<FeatureConfig>, Cloneable {

    public static FeatureConfig newConfig(FeatureId id) throws ProvisioningDescriptionException {
        final FeatureConfig config = new FeatureConfig(id.getSpec());
        for(Map.Entry<String, String> param : id.getParams().entrySet()) {
            config.setParam(param.getKey(), param.getValue());
        }
        return config;
    }

    public static FeatureConfig newConfig(String specName) throws ProvisioningDescriptionException {
        return new FeatureConfig(specName);
    }

    SpecId specId;
    Map<String, String> params = Collections.emptyMap();
    Set<String> unsetParams = Collections.emptySet();
    Set<String> resetParams = Collections.emptySet();
    Map<FeatureId, FeatureDependencySpec> deps = Collections.emptyMap();
    protected List<ConfigItem> items = Collections.emptyList();
    String parentRef;
    String origin;

    public FeatureConfig(FeatureConfig copy) {
        specId = copy.specId;
        origin = copy.origin;
        deps = copy.deps;
        parentRef = copy.parentRef;
        if(copy.params.size() > 1) {
            params = new HashMap<>(copy.params);
        } else {
            params = copy.params;
        }
        unsetParams = CollectionUtils.clone(copy.unsetParams);
        resetParams = CollectionUtils.clone(copy.resetParams);
        if(copy.items.isEmpty()) {
            items = copy.items;
            return;
        }
        if(copy.items.size() == 1) {
            if(copy.items.get(0).isGroup()) {
                items = copy.items;
                return;
            }
            items = Collections.singletonList(new FeatureConfig((FeatureConfig) copy.items.get(0)));
            return;
        }
        final List<ConfigItem> tmp = new ArrayList<>(copy.items.size());
        for (ConfigItem item : copy.items) {
            if (!item.isGroup()) {
                item = new FeatureConfig((FeatureConfig) item);
            }
            tmp.add(item);
        }
        items = Collections.unmodifiableList(tmp);
    }

    public FeatureConfig() {
    }

    public FeatureConfig(String specName) throws ProvisioningDescriptionException {
        this.specId = SpecId.fromString(specName);
    }

    public FeatureConfig(SpecId specId) throws ProvisioningDescriptionException {
        this.specId = specId;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    public FeatureConfig setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public FeatureConfig setSpecName(String specName) throws ProvisioningDescriptionException {
        this.specId = SpecId.fromString(specName);
        return this;
    }

    public SpecId getSpecId() {
        return this.specId;
    }

    public String getParentRef() {
        return this.parentRef;
    }

    public FeatureConfig setParentRef(String parentRef) {
        this.parentRef = parentRef;
        return this;
    }

    public boolean hasParams() {
        return !params.isEmpty();
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParam(String name) {
        return params.get(name);
    }

    public FeatureConfig setParam(String name, String value) {
        putParam(name, value);
        return this;
    }

    public String putParam(String name, String value) {
        final String prevValue = params.get(name);
        params = CollectionUtils.put(params, name, value);
        if(!unsetParams.isEmpty()) {
            unsetParams = CollectionUtils.remove(unsetParams, name);
        }
        if(!resetParams.isEmpty()) {
            resetParams = CollectionUtils.remove(resetParams, name);
        }
        return prevValue;
    }

    public FeatureConfig unsetParam(String name) {
        unsetParams = CollectionUtils.add(unsetParams, name);
        params = CollectionUtils.remove(params, name);
        if(!resetParams.isEmpty()) {
            resetParams = CollectionUtils.remove(resetParams, name);
        }
        return this;
    }

    public boolean hasUnsetParams() {
        return !unsetParams.isEmpty();
    }

    public Set<String> getUnsetParams() {
        return unsetParams;
    }

    public FeatureConfig resetParam(String name) {
        resetParams = CollectionUtils.add(resetParams, name);
        params = CollectionUtils.remove(params, name);
        if(!unsetParams.isEmpty()) {
            unsetParams = CollectionUtils.remove(unsetParams, name);
        }
        return this;
    }

    public boolean hasResetParams() {
        return !resetParams.isEmpty();
    }

    public Set<String> getResetParams() {
        return resetParams;
    }

    public FeatureConfig addFeatureDep(FeatureId featureId) throws ProvisioningDescriptionException {
        return addFeatureDep(FeatureDependencySpec.create(featureId));
    }

    public FeatureConfig addFeatureDep(FeatureDependencySpec dep) throws ProvisioningDescriptionException {
        if(deps.containsKey(dep.getFeatureId())) {
            throw new ProvisioningDescriptionException("Duplicate dependency on " + dep.getFeatureId());
        }
        deps = CollectionUtils.putLinked(deps, dep.getFeatureId(), dep);
        return this;
    }

    public boolean hasFeatureDeps() {
        return !deps.isEmpty();
    }

    public Collection<FeatureDependencySpec> getFeatureDeps() {
        return deps.values();
    }

    public Set<FeatureId> getFeatureDepIds() {
        return deps.keySet();
    }

    @Override
    public boolean hasItems() {
        return !items.isEmpty();
    }

    @Override
    public List<ConfigItem> getItems() {
        return items;
    }

    @Override
    public FeatureConfig addConfigItem(ConfigItem item) {
        items = CollectionUtils.add(items, item);
        return this;
    }

    @Override
    public boolean isResetFeaturePackOrigin() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deps == null) ? 0 : deps.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        result = prime * result + ((parentRef == null) ? 0 : parentRef.hashCode());
        result = prime * result + ((resetParams == null) ? 0 : resetParams.hashCode());
        result = prime * result + ((specId == null) ? 0 : specId.hashCode());
        result = prime * result + ((unsetParams == null) ? 0 : unsetParams.hashCode());
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
        FeatureConfig other = (FeatureConfig) obj;
        if (deps == null) {
            if (other.deps != null)
                return false;
        } else if (!deps.equals(other.deps))
            return false;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        if (items == null) {
            if (other.items != null)
                return false;
        } else if (!items.equals(other.items))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        if (parentRef == null) {
            if (other.parentRef != null)
                return false;
        } else if (!parentRef.equals(other.parentRef))
            return false;
        if (resetParams == null) {
            if (other.resetParams != null)
                return false;
        } else if (!resetParams.equals(other.resetParams))
            return false;
        if (specId == null) {
            if (other.specId != null)
                return false;
        } else if (!specId.equals(other.specId))
            return false;
        if (unsetParams == null) {
            if (other.unsetParams != null)
                return false;
        } else if (!unsetParams.equals(other.unsetParams))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(specId);
        if(origin != null) {
            buf.append(" origin=").append(origin);
        }
        if (!params.isEmpty()) {
            buf.append(' ');
            StringUtils.append(buf, params.entrySet());
        }
        if (!unsetParams.isEmpty()) {
            buf.append(" unset=");
            StringUtils.append(buf, unsetParams);
        }
        if (!resetParams.isEmpty()) {
            buf.append(" reset=");
            StringUtils.append(buf, resetParams);
        }
        if(parentRef != null) {
            buf.append(" parentRef=").append(parentRef);
        }
        if(!deps.isEmpty()) {
            buf.append(" dependencies=");
            StringUtils.append(buf, deps.values());
        }
        if(!items.isEmpty()) {
            buf.append(" items=");
            StringUtils.appendList(buf, items);
        }
        return buf.append(']').toString();
    }
}
