/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.spec;

import org.jboss.galleon.ProvisioningDescriptionException;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureDependencySpec {

    public static FeatureDependencySpec create(FeatureId featureId) throws ProvisioningDescriptionException {
        return create(featureId, null, false);
    }

    public static FeatureDependencySpec create(FeatureId featureId, String origin) throws ProvisioningDescriptionException {
        return create(featureId, origin, false);
    }

    public static FeatureDependencySpec create(FeatureId featureId, boolean include) throws ProvisioningDescriptionException {
        return create(featureId, null, include);
    }

    public static FeatureDependencySpec create(FeatureId featureId, String origin, boolean include) throws ProvisioningDescriptionException {
        return new FeatureDependencySpec(featureId, origin, include);
    }

    final String origin;
    final FeatureId featureId;
    final boolean include;

    private FeatureDependencySpec(FeatureId featureId, String origin, boolean include) throws ProvisioningDescriptionException {
        this.featureId = featureId;
        this.origin = origin;
        this.include = include;
    }

    public String getOrigin() {
        return origin;
    }

    public FeatureId getFeatureId() {
        return featureId;
    }

    public boolean isInclude() {
        return include;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((featureId == null) ? 0 : featureId.hashCode());
        result = prime * result + (include ? 1231 : 1237);
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
        FeatureDependencySpec other = (FeatureDependencySpec) obj;
        if (origin == null) {
            if (other.origin != null)
                return false;
        } else if (!origin.equals(other.origin))
            return false;
        if (featureId == null) {
            if (other.featureId != null)
                return false;
        } else if (!featureId.equals(other.featureId))
            return false;
        if (include != other.include)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(origin != null) {
            buf.append(" origin=").append(origin);
        }
        buf.append(" feature=").append(featureId);
        if(include) {
            buf.append(" auto-includes");
        }
        return buf.append(']').toString();
    }
}
