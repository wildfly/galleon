/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.installation.universe.names;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.IoUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningManagerUniverseManagementTestCase {

    protected Path home;

    @Before
    public void setup() throws Exception {
        home = Files.createTempDirectory("gln-test");
    }

    @After
    public void cleanup() throws Exception {
        if(home != null) {
            IoUtils.recursiveDelete(home);
        }
    }

    @Test
    public void testAddRemoveUniverseToFreshDir() throws Exception {
        Assert.assertNull(getProvisioningConfig());

        ProvisioningManager pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.addUniverse("universe1", new UniverseSpec("factory1", "location1"));

        ProvisioningConfig config = getProvisioningConfig();
        assertNotNull(config);
        assertFalse(config.hasDefaultUniverse());
        Map<String, UniverseSpec> universes = config.getUniverseNamedSpecs();
        assertEquals(1, universes.size());
        assertTrue(universes.containsKey("universe1"));

        pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.addUniverse("universe2", new UniverseSpec("factory2", "location2"));

        config = getProvisioningConfig();
        assertNotNull(config);
        assertFalse(config.hasDefaultUniverse());
        universes = config.getUniverseNamedSpecs();
        assertEquals(2, universes.size());
        assertTrue(universes.containsKey("universe1"));
        assertTrue(universes.containsKey("universe2"));

        pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.removeUniverse("universe1");

        config = getProvisioningConfig();
        assertNotNull(config);
        assertFalse(config.hasDefaultUniverse());
        universes = config.getUniverseNamedSpecs();
        assertEquals(1, universes.size());
        assertTrue(universes.containsKey("universe2"));

        pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.removeUniverse("universe2");
        config = getProvisioningConfig();
        assertFalse(config.hasDefaultUniverse());
        assertTrue(config.getUniverseNamedSpecs().isEmpty());
    }

    @Test
    public void testSetUnsetDefaultUniverseOnFreshDir() throws Exception {
        Assert.assertNull(getProvisioningConfig());

        ProvisioningManager pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.setDefaultUniverse(new UniverseSpec("factory1", "location1"));

        ProvisioningConfig config = getProvisioningConfig();
        assertNotNull(config);
        Map<String, UniverseSpec> universes = config.getUniverseNamedSpecs();
        assertEquals(0, universes.size());
        assertTrue(config.hasDefaultUniverse());
        assertEquals(new UniverseSpec("factory1", "location1"), config.getDefaultUniverse());

        pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.setDefaultUniverse(new UniverseSpec("factory2", "location1"));

        config = getProvisioningConfig();
        assertNotNull(config);
        universes = config.getUniverseNamedSpecs();
        assertEquals(0, universes.size());
        assertTrue(config.hasDefaultUniverse());
        assertEquals(new UniverseSpec("factory2", "location1"), config.getDefaultUniverse());

        pm = ProvisioningManager.builder().setInstallationHome(home).build();
        pm.removeUniverse(null);
        config = getProvisioningConfig();
        assertNotNull(config);
        universes = config.getUniverseNamedSpecs();
        assertEquals(0, universes.size());
        assertFalse(config.hasDefaultUniverse());
    }

    protected ProvisioningConfig getProvisioningConfig() throws Exception {
        return ProvisioningManager.builder().setInstallationHome(home).build().getProvisioningConfig();
    }
}
