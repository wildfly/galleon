/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
class ConfigFeatureBranch {

    final Object id;
    final boolean anonymous; // anonymous branch is a branch with the default incremental id
    private List<ResolvedFeature> list = new ArrayList<>();
    private boolean batch;
    private Set<ConfigFeatureBranch> deps = Collections.emptySet();
    private boolean ordered;
    private boolean fkBranch;
    private boolean specBranch;

    ConfigFeatureBranch(int index, boolean batch) {
        this.id = index;
        this.batch = batch;
        this.anonymous = true;
    }

    ConfigFeatureBranch(Object id, boolean batch) {
        this.id = id;
        this.batch = batch;
        this.anonymous = false;
    }

    List<ResolvedFeature> getFeatures() {
        return list;
    }

    boolean isEmpty() {
        return list.isEmpty();
    }

    boolean isBatch() {
        return batch;
    }

    void setBatch(boolean batch) throws ProvisioningException {
        if(!list.isEmpty()) {
            throw new ProvisioningException("Can't start batch in the middle of a branch");
        }
        this.batch = batch;
    }

    void setFkBranch() throws ProvisioningException {
        fkBranch = true;
    }

    boolean isFkBranch() {
        return fkBranch;
    }

    void setSpecBranch() throws ProvisioningException {
        specBranch = true;
    }

    boolean isSpecBranch() {
        return specBranch;
    }

    boolean hasDeps() {
        return !deps.isEmpty();
    }

    Set<ConfigFeatureBranch> getDeps() {
        return deps;
    }

    boolean dependsOn(ConfigFeatureBranch branch) {
        return deps.contains(branch);
    }

    void add(ResolvedFeature feature) throws ProvisioningException {
        feature.setBranch(this);
        list.add(feature);
    }

    void addBranchDep(final ConfigFeatureBranch dep) {
        deps = CollectionUtils.add(deps, dep);
    }

    boolean isOrdered() {
        return ordered;
    }

    void ordered() {
        ordered = true;
        if(list.isEmpty()) {
            return;
        }
        ResolvedFeature feature = list.get(0);
        feature.startBranch();
        final int size = list.size();
        if (batch && size > 1) {
            feature.startBatch();
        }
        feature = list.get(list.size() - 1);
        feature.endBranch();
        if (batch && size > 1) {
            feature.endBatch();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        ConfigFeatureBranch other = (ConfigFeatureBranch) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[id=").append(id);
        if(!deps.isEmpty()) {
            buf.append(" deps=");
            final Iterator<ConfigFeatureBranch> i = deps.iterator();
            buf.append(i.next().id);
            while(i.hasNext()) {
                buf.append(',').append(i.next().id);
            }
        }
        return buf.append(']').toString();
    }
}
