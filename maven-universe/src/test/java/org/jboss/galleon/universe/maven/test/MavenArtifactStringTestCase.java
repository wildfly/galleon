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

import org.jboss.galleon.universe.maven.MavenArtifact;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactStringTestCase {

    @Test
    public void testGavToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:jar:version", new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version").toString());
    }

    @Test
    public void testGavFromString() throws Exception {
        Assert.assertEquals(new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version"), MavenArtifact.fromString("groupId:artifactId:version"));
    }

    @Test
    public void testGARangeToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:jar:[1.0,2.0)", new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersionRange("[1.0,2.0)").toString());
    }

    @Test
    public void testGARangeFromString() throws Exception {
        Assert.assertEquals(new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersionRange("[1.0,2.0)"), MavenArtifact.fromString("groupId:artifactId:[1.0,2.0)"));
    }

    @Test
    public void testGavExtToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:zip:version", new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version").setExtension("zip").toString());
    }

    @Test
    public void testGavExtFromString() throws Exception {
        Assert.assertEquals(new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version").setExtension("zip"), MavenArtifact.fromString("groupId:artifactId:zip:version"));
    }

    @Test
    public void testGavExtClassifierToString() throws Exception {
        Assert.assertEquals("groupId:artifactId:zip:classifier:version", new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version").setExtension("zip").setClassifier("classifier").toString());
    }

    @Test
    public void testGavExtClassifierFromString() throws Exception {
        Assert.assertEquals(new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").setVersion("version").setExtension("zip").setClassifier("classifier"), MavenArtifact.fromString("groupId:artifactId:zip:classifier:version"));
    }

    @Test
    public void testGaToString() throws Exception {
        Assert.assertEquals("groupId:artifactId", new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId").toString());
    }

    @Test
    public void testGaFromString() throws Exception {
        Assert.assertEquals(new MavenArtifact().setGroupId("groupId").setArtifactId("artifactId"), MavenArtifact.fromString("groupId:artifactId"));
    }
}
