/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jdenise
 */
public class CoreVersion {

    public static String getVersion() {
        String version = getConfigEntry("jboss-galleon-version");
        return version;
    }

    private static String getConfigEntry(String entry) {

        String prop = System.getProperty(entry);
        if (prop != null) {
            return prop;
        }

        InputStream stream = CoreVersion.class.getResourceAsStream("galleon.properties");
        if (stream == null) {
            return null;
        }
        try {
            try {
                Properties properties = new Properties();
                properties.load(stream);
                String value = properties.getProperty(entry);
                return value;
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isSupportedVersion(String inFeaturePack) {
        return getVersion().compareTo(inFeaturePack) >= 0;
    }
}
