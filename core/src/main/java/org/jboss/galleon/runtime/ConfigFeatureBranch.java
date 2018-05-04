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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;

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
    private ResolvedSpecId specId;

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
            throw new ProvisioningException("Can't start batch in middle of the branch");
        }
        this.batch = batch;
    }

    void setFkBranch() throws ProvisioningException {
        //if(!list.isEmpty()) {
        //    throw new ProvisioningException("Can't start a foreign key branch in middle of the branch");
        //}
        fkBranch = true;
    }

    boolean isFkBranch() {
        return fkBranch;
    }

    void setSpecId(ResolvedSpecId specId) throws ProvisioningException {
//        if(!list.isEmpty()) {
//            throw new ProvisioningException("Can't start a spec branch in the middle of a branch");
//        }
        this.specId = specId;
    }

    boolean isSpecBranch() {
        return specId != null;
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

    void add(ResolvedFeature feature) {
        feature.branch = this;
        //System.out.println("ConfiguredFeatureBranch.add " + this + " " + feature.id + " " + feature.branchDeps);
        list.add(feature);
        if(feature.branchDeps.isEmpty() ||
                feature.branchDeps.size() == 1 && feature.branchDeps.containsKey(this)) {
            return;
        }
        if(deps.isEmpty()) {
            deps = new HashSet<>(feature.branchDeps.size());
        }
        final Iterator<ConfigFeatureBranch> iter = feature.branchDeps.keySet().iterator();
        while(iter.hasNext()) {
            final ConfigFeatureBranch dep = iter.next();
            if(id != dep.id) {
                deps.add(dep);
            }
        }
        //System.out.println("  updated deps " + deps);
    }

    boolean isOrdered() {
        return ordered;
    }

    void ordered() {
        ordered = true;
        if(!list.isEmpty()) {
            ResolvedFeature feature = list.get(0);
            feature.startBranch();
            final int size = list.size();
            if(batch && size > 1) {
                feature.startBatch();
            }
            feature = list.get(list.size() - 1);
            feature.endBranch();
            if(batch && size > 1) {
                feature.endBatch();
            }
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
