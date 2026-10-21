/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author jdenise
 */
public class APIVersion {

    public static String getVersion() {
        String version = getConfigEntry("jboss-galleon-version");
        return version;
    }

    private static String getConfigEntry(String entry) {

        String prop = System.getProperty(entry);
        if (prop != null) {
            return prop;
        }

        InputStream stream = APIVersion.class.getResourceAsStream("galleon.properties");
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

    public static String checkForLatestVersionURL() {
        JsonNode newer = getNewerRelease();
        if (newer != null) {
            return newer.get("html_url").asText();
        }
        return null;
    }

    public static String checkForLatestVersion() {
        JsonNode newer = getNewerRelease();
        if (newer != null) {
            return newer.get("tag_name").asText();
        }
        return null;
    }

    private static JsonNode getNewerRelease() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode latest = mapper.readTree(new URL("https://api.github.com/repos/wildfly/galleon/releases/latest"));
            JsonNode tag = latest.get("tag_name");
            if (tag != null) {
                String value = tag.asText();
                if (value.compareTo(APIVersion.getVersion()) > 0) {
                    return latest;
                }
            }
        } catch (Exception ex) {
            //System.err.println("Error while retrieving latest Galleon version: " + ex.getMessage());
        }
        return null;
    }
}
