/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.universe.maven.test;

import org.jboss.galleon.universe.maven.repo.MavenArtifactVersion;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersionRange;
import org.jboss.galleon.universe.maven.repo.MavenArtifactVersionRangeParser;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactVersionRangeTestCase {

    private final MavenArtifactVersionRangeParser parser = new MavenArtifactVersionRangeParser();

    @Test
    public void testUpToVersionExclusive() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(,1.0)");
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0.Alpha4")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.1")));

        range = parser.parseRange("(,1.0-alpha3)");
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0.Alpha2")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.0.Alpha3")));
    }

    @Test
    public void testUpToVersionInclusive() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(,1.0]");
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.1")));
    }

    @Test
    public void testSpecificVersion() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("[1.0]");
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.0.Alpha")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.1")));
    }

    @Test
    public void testInclusiveRange() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("[1.2,1.3]");
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.1.0.Final")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.2.0.Alpha")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.2.0")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.2.5.Alpha")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.3.0.Final")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.3.1.Final")));
    }

    @Test
    public void testExclusiveRange() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(1.2,1.3)");
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.2.0.Final")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.2.0.Alpha")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.2.5.Alpha")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.3.0.Alpha")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.3.0.Final")));
    }

    @Test
    public void testFromVersionInclusive() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("[1.0,)");
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.1")));
    }

    @Test
    public void testFromVersionExclusive() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(1.0,)");
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.1")));
    }

    @Test
    public void testExcludedRange() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(,1.0],[1.2,)");
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("0.5")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.0.1")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.2.CR")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.2.0")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("5.5.5")));
    }

    @Test
    public void testExcludedVersion() throws Exception {
        MavenArtifactVersionRange range = parser.parseRange("(,1.1),(1.1,)");
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.0.0")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.1.Final-SNAPSHOT")));
        Assert.assertFalse(range.includesVersion(new MavenArtifactVersion("1.1.Final")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("1.1.1.Alpha1-SNAPSHOT")));
        Assert.assertTrue(range.includesVersion(new MavenArtifactVersion("5.5.5")));
    }
}
