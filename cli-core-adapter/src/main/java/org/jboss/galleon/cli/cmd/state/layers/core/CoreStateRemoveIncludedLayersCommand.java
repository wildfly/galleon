/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.layers.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.layers.AbstractLayersCommand;
import org.jboss.galleon.cli.cmd.state.layers.StateRemoveIncludedLayersCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateRemoveIncludedLayersCommand extends CoreAbstractLayersCommand<StateRemoveIncludedLayersCommand> {

    public static class LayersCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

        @Override
        public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
            AbstractLayersCommand cmd = (AbstractLayersCommand) invoc.getCommand();
            ConfigId config = cmd.getConfig();
            if (config == null) {
                return Collections.emptyList();
            }
            ConfigModel cModel = context.getState().getConfig().getDefinedConfig(config);
            if (cModel == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(cModel.getIncludedLayers());
        }

    }

    @Override
    protected void runCommand(ProvisioningSession session, State state, StateRemoveIncludedLayersCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.removeIncludedLayersConfiguration(session, getConfiguration(state, command), command.getLayers().split(",+"));
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.removeFailed(), ex);
        }
    }
}
