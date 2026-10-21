/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.config.mvn;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author jdenise@redhat.com
 */
public class MavenRemoteRepository {
    private final String name;
    private final String url;
    private final String type;
    private final String releaseUpdatePolicy;
    private final String snapshotUpdatePolicy;
    private final Boolean enableSnapshot;
    private final Boolean enableRelease;
    MavenRemoteRepository(String name, String type, String url) {
        this.name = name;
        this.url = url;
        this.type = type;
        // Will be set with default configured policies.
        this.releaseUpdatePolicy = null;
        this.snapshotUpdatePolicy = null;
        this.enableRelease = null;
        this.enableSnapshot = null;
    }

    public MavenRemoteRepository(String name, String type, String releaseUpdatePolicy,
            String snapshotUpdatePolicy, Boolean enableRelease,
            Boolean enableSnapshot, String url) throws ProvisioningException {
        this.name = name;
        this.url = url;
        this.type = type;
        MavenConfig.validatePolicy(releaseUpdatePolicy);
        this.releaseUpdatePolicy = releaseUpdatePolicy;
        MavenConfig.validatePolicy(snapshotUpdatePolicy);
        this.snapshotUpdatePolicy = snapshotUpdatePolicy;
        this.enableRelease = enableRelease;
        this.enableSnapshot = enableSnapshot;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the updateReleasePolicy
     */
    public String getReleaseUpdatePolicy() {
        return releaseUpdatePolicy;
    }

    /**
     * @return the updateSnapshotPolicy
     */
    public String getSnapshotUpdatePolicy() {
        return snapshotUpdatePolicy;
    }

    /**
     * @return the enableSnapshot
     */
    public Boolean getEnableSnapshot() {
        return enableSnapshot;
    }

    /**
     * @return the enableRelease
     */
    public Boolean getEnableRelease() {
        return enableRelease;
    }

}
