/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.layers.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.layers.AbstractLayersCommand;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;

/**
 *
 * @author jdenise@redhat.com
 */
abstract class CoreAbstractLayersCommand<T extends AbstractLayersCommand> extends CoreAbstractStateCommand<T> {

    protected ConfigInfo getConfiguration(State state, AbstractLayersCommand command) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + command.getConfiguration() + PathParser.PATH_SEPARATOR;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        ConfigInfo ci = consumer.getConfig();
        if (ci == null) {
            throw new ProvisioningException("Not a valid config " + command.getConfiguration());
        }
        return ci;
    }
}
