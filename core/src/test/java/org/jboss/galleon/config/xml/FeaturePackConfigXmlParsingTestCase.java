/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config.xml;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.xml.ConfigXmlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
public class FeaturePackConfigXmlParsingTestCase {
    @Test
    public void testSimple() throws Exception {
        ConfigModel result = parseConfig("featurepack-config.xml");
        Assert.assertNotNull(result);
        assertEquals(ConfigModel.builder("standalone", "standalone.xml").addConfigItem(FeatureConfig.newConfig("server-root").setParam("name", "test")).build(),
                result);
    }


    private static ConfigModel parseConfig(String xml) throws Exception {
        final Path path = getResource("xml/config/" + xml);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return ConfigXmlParser.getInstance().parse(reader);
        }
    }

    private static Path getResource(String path) {
        java.net.URL resUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        Assert.assertNotNull("Resource " + path + " is not on the classpath", resUrl);
        try {
            return Paths.get(resUrl.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("Failed to get URI from URL", e);
        }
    }
}
