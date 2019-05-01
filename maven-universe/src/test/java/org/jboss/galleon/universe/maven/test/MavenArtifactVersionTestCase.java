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
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactVersionTestCase {

    @Test
    public void testNoQualifierComparison() throws Exception {

        Assert.assertEquals(0, new MavenArtifactVersion("1.0.0").compareTo(new MavenArtifactVersion("1")));
        Assert.assertEquals(0, new MavenArtifactVersion("1.0.0").compareTo(new MavenArtifactVersion("1.0")));
        Assert.assertTrue(new MavenArtifactVersion("1.0.0").compareTo(new MavenArtifactVersion("1.0.1")) < 0);
        Assert.assertTrue(new MavenArtifactVersion("1.1").compareTo(new MavenArtifactVersion("1.0.1")) > 0);
    }

    @Test
    public void testQualifierComparison() throws Exception {

        Assert.assertEquals(0, new MavenArtifactVersion("1.0.0.Alpha").compareTo(new MavenArtifactVersion("1-alpha")));
        Assert.assertEquals(0, new MavenArtifactVersion("1.0.0.Alpha").compareTo(new MavenArtifactVersion("1.0-alpha0")));
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Alpha").compareTo(new MavenArtifactVersion("1.0.0")) < 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Alpha1").compareTo(new MavenArtifactVersion("1.0.0.Alpha")) > 0);

        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Beta").compareTo(new MavenArtifactVersion("1.0.0.Alpha")) > 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Milestone").compareTo(new MavenArtifactVersion("1.0.0.Beta")) > 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.CR").compareTo(new MavenArtifactVersion("1.0.0.Milestone")) > 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.CR").compareTo(new MavenArtifactVersion("1.0.0.RC")) == 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0-SNAPSHOT").compareTo(new MavenArtifactVersion("1.0.0.CR")) > 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Final").compareTo(new MavenArtifactVersion("1.0.0-SNAPSHOT")) > 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Final").compareTo(new MavenArtifactVersion("1.0.0")) == 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Final").compareTo(new MavenArtifactVersion("1.0.0.GA")) == 0);
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.SP").compareTo(new MavenArtifactVersion("1.0.0.Final")) > 0);
    }

    @Test
    public void testSnapshots() {
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Alpha2").compareTo(new MavenArtifactVersion("1.0.0.Alpha2-SNAPSHOT")) > 0);
    }

    @Test
    public void testIsSnapshot() {
        Assert.assertFalse(new MavenArtifactVersion("1.0.0.Alpha2").isSnapshot());
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Alpha2-SNAPSHOT").isSnapshot());
        Assert.assertFalse(new MavenArtifactVersion("1.0").isSnapshot());
        Assert.assertFalse(new MavenArtifactVersion("1.0.ZSNAPSHOT").isSnapshot());
    }

    @Test
    public void testQualifierWeight() throws Exception {
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Beta").isQualifierHigher("alpha", false));
        Assert.assertFalse(new MavenArtifactVersion("1.0.0.Beta").isQualifierHigher("Beta", false));
        Assert.assertTrue(new MavenArtifactVersion("1.0.0.Beta").isQualifierHigher("Beta", true));

        Assert.assertFalse(new MavenArtifactVersion("1.0.0.Beta-SNAPSHOT").isQualifierHigher("Beta", true));
        Assert.assertFalse(new MavenArtifactVersion("1.0.0.Beta1-SNAPSHOT").isQualifierHigher("Beta", true));

        Assert.assertTrue(new MavenArtifactVersion("1.0.0.CR-SNAPSHOT").isQualifierHigher("Beta", true));
    }
}
