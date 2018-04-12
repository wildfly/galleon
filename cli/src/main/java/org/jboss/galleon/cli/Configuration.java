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
package org.jboss.galleon.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jdenise@redhat.com
 */
public class Configuration {

    private static final File DEFAULT_HISTORY_FILE = new File(System.getProperty("user.home"), ".pm-history");
    private final List<UniverseLocation> universes = new ArrayList<>();
    private String mavenRepositoryURL;
    private File historyFile = DEFAULT_HISTORY_FILE;

    private Configuration() {
    }

    public File getHistoryFile() {
        return historyFile;
    }

    public List<UniverseLocation> getUniversesLocations() {
        return Collections.unmodifiableList(universes);
    }

    public String getMavenRepositoryURL() {
        return mavenRepositoryURL;
    }

    public static Configuration parse() {
        // For now, no XML config.
        // TODO
        Configuration config = new Configuration();
        config.universes.add(UniverseLocation.DEFAULT);
        return config;
    }
}
