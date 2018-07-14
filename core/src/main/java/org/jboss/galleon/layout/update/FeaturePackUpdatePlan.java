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

package org.jboss.galleon.layout.update;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackUpdatePlan {

    public class Request {

        private Request() {
        }

        public FeaturePackLocation getInstalledLocation() {
            return installedFpl;
        }

        public boolean hasInstalledPatches() {
            return !installedPatches.isEmpty();
        }

        public Set<FPID> getInstalledPatches() {
            return installedPatches;
        }

        public boolean isPatchInstalled(FPID patchId) {
            return installedPatches.contains(patchId);
        }

        public Request setNewLocation(FeaturePackLocation newLocation) {
            newFpl = newLocation;
            return this;
        }

        public Request addNewPatch(FPID patchId) {
            newPatches = CollectionUtils.add(newPatches, patchId);
            return this;
        }

        public FeaturePackUpdatePlan buildPlan() {
            newPatches = CollectionUtils.unmodifiable(newPatches);
            return FeaturePackUpdatePlan.this;
        }
    }

    public static Request request(FeaturePackLocation installedFpl) {
        return request(installedFpl, Collections.emptySet(), false);
    }

    public static Request request(FeaturePackLocation installedFpl, boolean transitive) {
        return request(installedFpl, Collections.emptySet(), transitive);
    }

    public static Request request(FeaturePackLocation installedFpl, Set<FPID> installedPatches) {
        return request(installedFpl, installedPatches, false);
    }

    public static Request request(FeaturePackLocation installedFpl, Set<FPID> installedPatches, boolean transitive) {
        return new FeaturePackUpdatePlan(installedFpl, installedPatches, transitive).newRequest();
    }

    private final boolean transitive;
    private final FeaturePackLocation installedFpl;
    private final Set<FPID> installedPatches;
    private FeaturePackLocation newFpl;
    private List<FPID> newPatches = Collections.emptyList();

    private FeaturePackUpdatePlan(FeaturePackLocation fpl, Set<FPID> patches, boolean transitive) {
        this.transitive = transitive;
        this.installedFpl = fpl;
        this.installedPatches = patches;
        this.newFpl = fpl;
    }

    private Request newRequest() {
        return new Request();
    }

    public boolean isTransitive() {
        return transitive;
    }

    public FeaturePackLocation getInstalledLocation() {
        return installedFpl;
    }

    public boolean hasInstalledPatches() {
        return !installedPatches.isEmpty();
    }

    public Set<FPID> getInstalledPatches() {
        return installedPatches;
    }

    public boolean isPatchInstalled(FPID patchId) {
        return installedPatches.contains(patchId);
    }

    public FeaturePackLocation getNewLocation() {
        return newFpl;
    }

    public boolean hasNewLocation() {
        return !installedFpl.equals(newFpl);
    }

    public boolean hasNewPatches() {
        return !newPatches.isEmpty();
    }

    public List<FPID> getNewPatches() {
        return newPatches;
    }

    public boolean isEmpty() {
        return !(hasNewLocation() || hasNewPatches());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((installedFpl == null) ? 0 : installedFpl.hashCode());
        result = prime * result + ((newFpl == null) ? 0 : newFpl.hashCode());
        result = prime * result + ((newPatches == null) ? 0 : newPatches.hashCode());
        result = prime * result + (transitive ? 1231 : 1237);
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
        FeaturePackUpdatePlan other = (FeaturePackUpdatePlan) obj;
        if (installedFpl == null) {
            if (other.installedFpl != null)
                return false;
        } else if (!installedFpl.equals(other.installedFpl))
            return false;
        if (newFpl == null) {
            if (other.newFpl != null)
                return false;
        } else if (!newFpl.equals(other.newFpl))
            return false;
        if (newPatches == null) {
            if (other.newPatches != null)
                return false;
        } else if (!newPatches.equals(other.newPatches))
            return false;
        if (transitive != other.transitive)
            return false;
        return true;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[update ");
        if(transitive) {
            buf.append("transitive ");
        }
        buf.append(installedFpl);
        if(hasNewLocation()) {
            buf.append(" to ").append(newFpl);
        }
        if(!newPatches.isEmpty()) {
            buf.append(" with ");
            StringUtils.append(buf, newPatches);
        }
        return buf.append(']').toString();
    }
}
