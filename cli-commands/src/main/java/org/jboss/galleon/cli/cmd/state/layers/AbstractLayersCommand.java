/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
