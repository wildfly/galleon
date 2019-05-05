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
package org.jboss.galleon.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * A set of changes that can be applied to a provisioned state.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningPlan {

    public static ProvisioningPlan builder() {
        return new ProvisioningPlan();
    }

    private Map<ProducerSpec, FeaturePackConfig> install = Collections.emptyMap();
    private Set<ProducerSpec> uninstall = Collections.emptySet();
    private Map<ProducerSpec, FeaturePackUpdatePlan> updates = Collections.emptyMap();

    public ProvisioningPlan install(FeaturePackLocation fpl) throws ProvisioningDescriptionException {
        return install(FeaturePackConfig.forLocation(fpl));
    }

    public ProvisioningPlan install(FeaturePackConfig fpConfig) throws ProvisioningDescriptionException {
        final ProducerSpec producer = fpConfig.getLocation().getProducer();
        if(uninstall.contains(producer) || updates.containsKey(producer)) {
            throw new ProvisioningDescriptionException(producer + " has already been added to the plan");
        }
        install = CollectionUtils.putLinked(install, producer, fpConfig);
        return this;
    }

    public ProvisioningPlan uninstall(ProducerSpec producer) throws ProvisioningDescriptionException {
        if(install.containsKey(producer) || updates.containsKey(producer)) {
            throw new ProvisioningDescriptionException(producer + " has already been added to the plan");
        }
        uninstall = CollectionUtils.add(uninstall, producer);
        return this;
    }

    public ProvisioningPlan update(FeaturePackUpdatePlan fpPlan) throws ProvisioningDescriptionException {
        final ProducerSpec producer = fpPlan.getInstalledLocation().getProducer();
        if(install.containsKey(producer) || uninstall.contains(producer)) {
            throw new ProvisioningDescriptionException(producer + " has already been added to the plan");
        }
        updates = CollectionUtils.putLinked(updates, producer, fpPlan);
        return this;
    }

    public boolean hasInstall() {
        return !install.isEmpty();
    }

    public boolean hasUninstall() {
        return !uninstall.isEmpty();
    }

    public boolean hasUpdates() {
        return !updates.isEmpty();
    }

    public boolean isEmpty() {
        return install.isEmpty() && updates.isEmpty() && uninstall.isEmpty();
    }

    public Collection<FeaturePackConfig> getInstall() {
        return install.values();
    }

    public Collection<ProducerSpec> getUninstall() {
        return uninstall;
    }

    public Collection<FeaturePackUpdatePlan> getUpdates() {
        return updates.values();
    }

    Map<ProducerSpec, FeaturePackUpdatePlan> getUpdateMap() {
        return updates;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((install == null) ? 0 : install.hashCode());
        result = prime * result + ((uninstall == null) ? 0 : uninstall.hashCode());
        result = prime * result + ((updates == null) ? 0 : updates.hashCode());
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
        ProvisioningPlan other = (ProvisioningPlan) obj;
        if (install == null) {
            if (other.install != null)
                return false;
        } else if (!install.equals(other.install))
            return false;
        if (uninstall == null) {
            if (other.uninstall != null)
                return false;
        } else if (!uninstall.equals(other.uninstall))
            return false;
        if (updates == null) {
            if (other.updates != null)
                return false;
        } else if (!updates.equals(other.updates))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(!install.isEmpty()) {
            buf.append("install ");
            StringUtils.append(buf, getInstall());
            buf.append(';');
        }
        if(!uninstall.isEmpty()) {
            if(buf.length() > 1) {
                buf.append(' ');
            }
            buf.append("uninstall ");
            StringUtils.append(buf, uninstall);
            buf.append(';');
        }
        if(!updates.isEmpty()) {
            if(buf.length() > 1) {
                buf.append(' ');
            }
            buf.append("update ");
            StringUtils.append(buf, updates.values());
            buf.append(';');
        }
        return buf.toString();
    }
}
