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
package org.jboss.galleon.cli.cmd.state.layers;

import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.configuration.ProvisionedConfigurationCompleter;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractLayersCommand extends AbstractStateCommand {

    @Argument(required = true, description = HelpDescriptions.CONFIGURATION_FULL_NAME,
            completer = ProvisionedConfigurationCompleter.class)
    private String configuration;

    public ConfigId getConfig() {
        if (getConfiguration() == null || getConfiguration().isEmpty()) {
            return null;
        }
        int sep = getConfiguration().indexOf("/");
        if (sep == -1 || sep == getConfiguration().length() - 1) {
            return null;
        }
        return new ConfigId(getConfiguration().substring(0, sep), getConfiguration().substring(sep + 1));
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }
}
