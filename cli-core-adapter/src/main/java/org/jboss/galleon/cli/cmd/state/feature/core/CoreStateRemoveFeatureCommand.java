/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.feature.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.feature.StateRemoveFeatureCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveFeatureCommand extends CoreAbstractStateCommand<StateRemoveFeatureCommand> {

    @Override
    protected void runCommand(ProvisioningSession invoc, State session, StateRemoveFeatureCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH
                    + (command.getFeature().endsWith("" + PathParser.PATH_SEPARATOR) ? command.getFeature() : command.getFeature() + PathParser.PATH_SEPARATOR);
            FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(session.getContainer(), false);
            PathParser.parse(path, consumer);
            ConfigInfo ci = consumer.getConfig();
            if (ci == null) {
                throw new ProvisioningException("Not a valid configuration " + command.getFeature());
            }
            Group grp = consumer.getCurrentNode(path);
            if (grp == null) {
                throw new ProvisioningException("Not a valid feature " + command.getFeature());
            }
            FeatureInfo fi = grp.getFeature();
            if (fi == null) {
                throw new ProvisioningException("Not a valid feature " + command.getFeature());
            }

            session.removeFeature(invoc, ci, fi);
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.removeFeatureFailed(), ex);
        }
    }
}
