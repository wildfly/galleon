/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.maven.plugin.util;

import org.jboss.galleon.config.ConfigId;

/**
 * Simple wrapper for configuration ids.
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
public class ConfigurationId {

    private String name;
    private String model;

    public ConfigurationId() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ConfigId getId() {
        return new ConfigId(model, name);
    }

    public boolean isModelOnly() {
        return name == null || name.isEmpty();
    }
}
