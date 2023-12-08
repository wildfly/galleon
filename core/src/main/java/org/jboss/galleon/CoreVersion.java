/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
