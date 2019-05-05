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
