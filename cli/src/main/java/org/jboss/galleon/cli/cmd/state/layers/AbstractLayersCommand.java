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
package org.jboss.galleon.cli.cmd.state.layers;

import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.configuration.ProvisionedConfigurationCompleter;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
abstract class AbstractLayersCommand extends AbstractStateCommand {

    @Argument(required = true, description = HelpDescriptions.CONFIGURATION_FULL_NAME,
            completer = ProvisionedConfigurationCompleter.class)
    private String configuration;

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }

    ConfigId getConfig() {
        if (configuration == null || configuration.isEmpty()) {
            return null;
        }
        int sep = configuration.indexOf("/");
        if (sep == -1 || sep == configuration.length() - 1) {
            return null;
        }
        return new ConfigId(configuration.substring(0, sep), configuration.substring(sep + 1));
    }

    protected ConfigInfo getConfiguration(State state) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + configuration + PathParser.PATH_SEPARATOR;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        ConfigInfo ci = consumer.getConfig();
        if (ci == null) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return ci;
    }
}
