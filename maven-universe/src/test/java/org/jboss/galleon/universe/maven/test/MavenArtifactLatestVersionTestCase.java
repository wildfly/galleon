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
