/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
