/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.spec;

import org.jboss.galleon.repo.RepositoryArtifactResolver;

/**
 * Information necessary to resolve a plugin from a remote repository.
 *
 * Plugin ID is important when multiple feature-packs include different
 * versions of the same plugin when only one of them should be effective.
 * The ID is used then to determine which plugins are conflicting.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackPlugin {

    public static final String DEFAULT_REPO_ID = RepositoryArtifactResolver.ID_PREFIX + "maven";

    public static boolean isDefaultRepoId(String repoId) {
        return DEFAULT_REPO_ID.equals(repoId);
    }

    public static FeaturePackPlugin getInstance(String id, String location) {
        return getInstance(id, DEFAULT_REPO_ID, location);
    }

    public static FeaturePackPlugin getInstance(String id, String repoId, String location) {
        return new FeaturePackPlugin(repoId, location, id);
    }

    private final String repoId;
    private final String location;
    private final String id;

    private FeaturePackPlugin(String repoId, String location, String id) {
        this.repoId = repoId;
        this.location = location;
        this.id = id;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((repoId == null) ? 0 : repoId.hashCode());
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
        FeaturePackPlugin other = (FeaturePackPlugin) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (repoId == null) {
            if (other.repoId != null)
                return false;
        } else if (!repoId.equals(other.repoId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(id).append(' ');
        if(!isDefaultRepoId(repoId)) {
            buf.append(repoId).append(' ');
        }
        return buf.append(location).append(']').toString();
    }
}
