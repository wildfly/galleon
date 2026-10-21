/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.config.mvn;

import org.eclipse.aether.RepositorySystem;
import org.jboss.galleon.cli.Util;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class MvnOfflineTestCase {
    @Test
    public void test() throws Exception {
        RepositorySystem system = Util.newRepositorySystem();
        MavenConfig config = new MavenConfig();
        MavenCliSettings settings = new MavenCliSettings(config, system, null);
        assertFalse(settings.getSession().isOffline());
        config.enableOffline(true);
        settings = new MavenCliSettings(config, system, null);
        assertTrue(settings.getSession().isOffline());
    }
}
