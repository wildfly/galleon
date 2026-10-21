/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactLatestVersionTestCase {

    @Test
    public void testLatestFinalVersionNoQualifier() throws Exception {
        assertLatestVersion("1.0.0.Final", null, "1.0.0.Alpha1", "1.0.0.Final", "0.9.9.Final", "1.0.0.Beta2", "1.0.0.Final-SNAPSHOT");
    }

    @Test
    public void testLatestFinalVersionWithQualifier() throws Exception {
        assertLatestVersion("1.0.0.Final", "final", "1.0.0.Alpha1", "1.0.0.Final", "0.9.9.Final", "1.0.0.Beta2", "1.0.0.Final-SNAPSHOT");
    }

    @Test
    public void testNoFinalVersionForNoQualifier() throws Exception {
        assertNoLatestVersion(null, "1.0.0.Alpha1", "1.0.0.Beta1", "1.0.0.Final-SNAPSHOT");
    }

    @Test
    public void testNoFinalVersionForFinalQualifier() throws Exception {
        assertNoLatestVersion("final", "1.0.0.Alpha1", "1.0.0.Beta1", "1.0.0.Final-SNAPSHOT");
    }

    @Test
    public void testLatestAlpha() throws Exception {
        assertLatestVersion("1.0.0.Alpha3", "alpha", "1.0.0.Alpha1", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Alpha3", "1.0.0.Alpha2");
    }

    @Test
    public void testNoLatestAlpha() throws Exception {
        assertNoLatestVersion("alpha", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Final-SNAPSHOT");
    }

    @Test
    public void testAlphaQualifierAllowsBeta() throws Exception {
        assertLatestVersion("1.0.0.Beta1", "alpha", "1.0.0.Alpha1", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Beta1", "1.0.0.Beta2-SNAPSHOT", "1.0.0.Alpha3", "1.0.0.Alpha2");
    }

    @Test
    public void testAlphaQualifierAllowsFinal() throws Exception {
        assertLatestVersion("1.0.0.Final", "alpha", "1.0.0.Alpha1", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Beta1", "1.0.0.Beta2-SNAPSHOT", "1.0.0.Alpha3", "1.0.0.Alpha2", "1.0.0.Final");
    }

    @Test
    public void testLatestBeta() throws Exception {
        assertLatestVersion("1.0.0.Beta2", "beta", "1.0.0.Alpha1", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Beta2", "1.0.0.Alpha2", "1.0.0.Beta1", "1.0.0.Beta3-SNAPSHOT");
    }

    @Test
    public void testNoLatestBeta() throws Exception {
        assertNoLatestVersion("beta", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Final-SNAPSHOT", "1.0.0.Alpha3");
    }

    @Test
    public void testBetaQualifierAllowsFinal() throws Exception {
        assertLatestVersion("1.0.0.Final", "beta", "1.0.0.Alpha1", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Beta1", "1.0.0.Beta2-SNAPSHOT", "1.0.0.Alpha2", "1.0.0.Final");
    }

    @Test
    public void testLatestAlphaSnapshot() throws Exception {
        assertLatestVersion("1.0.0.Alpha4-SNAPSHOT", "snapshot", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Alpha3-SNAPSHOT");
    }

    @Test
    public void testLatestBetaSnapshot() throws Exception {
        assertLatestVersion("1.0.0.Beta3-SNAPSHOT", "snapshot", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Beta3-SNAPSHOT");
    }

    @Test
    public void testLatestFinalSnapshot() throws Exception {
        assertLatestVersion("1.0.0.Final-SNAPSHOT", "snapshot", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha4-SNAPSHOT", "1.0.0.Final-SNAPSHOT", "1.0.0.Beta3-SNAPSHOT");
    }

    @Test
    public void testSnapshotQualifierAllowsAlphaRelease() throws Exception {
        assertLatestVersion("1.0.0.Alpha2", "snapshot", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Alpha2", "1.0.0.Alpha2-SNAPSHOT");
    }

    @Test
    public void testBetaSnapshotIsHigherThanAlphaRelease() throws Exception {
        assertLatestVersion("1.0.0.Beta1-SNAPSHOT", "snapshot", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha2");
    }

    @Test
    public void testSnapshotQualifierAllowsBetaRelease() throws Exception {
        assertLatestVersion("1.0.0.Beta1", "snapshot", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha2", "1.0.0.Beta1");
    }

    @Test
    public void testFinalSnapshotQualifierIsHigherThanBetaRelease() throws Exception {
        assertLatestVersion("1.0.0.Final-SNAPSHOT", "snapshot", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Final-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha2", "1.0.0.Beta1");
    }

    @Test
    public void testSnapshotQualifierAllowsFinalRelease() throws Exception {
        assertLatestVersion("1.0.0.Final", "snapshot", "1.0.0.Final", "1.0.0.Alpha1-SNAPSHOT", "1.0.0.Final-SNAPSHOT", "1.0.0.Beta1-SNAPSHOT", "1.0.0.Alpha2", "1.0.0.Beta1");
    }

    private void assertLatestVersion(String expected, String lowestQualifier, String... versions) throws MavenUniverseException {
        assertEquals(new MavenArtifactVersion(expected), MavenArtifactVersion.getLatest(Arrays.asList(versions), lowestQualifier));
    }

    private void assertNoLatestVersion(String lowestQualifier, String... versions) throws MavenUniverseException {
        assertNull(MavenArtifactVersion.getLatest(Arrays.asList(versions), lowestQualifier));
    }
}
