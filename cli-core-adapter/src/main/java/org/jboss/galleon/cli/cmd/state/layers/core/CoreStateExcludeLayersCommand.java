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
import org.jboss.galleon.cli.cmd.state.layers.StateExcludeLayersCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateExcludeLayersCommand extends CoreAbstractLayersCommand<StateExcludeLayersCommand> {

    public static class LayersCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

        @Override
        public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession session) {
            AbstractLayersCommand cmd = (AbstractLayersCommand) invoc.getCommand();
            ConfigId config = cmd.getConfig();
            if (config == null) {
                return Collections.emptyList();
            }
            List<ConfigInfo> configs = session.getState().getContainer().getFinalConfigs().get(config.getModel());
            ConfigInfo targetConfig = null;
            if (configs != null) {
                for (ConfigInfo ci : configs) {
                    if (ci.getId().equals(config)) {
                        targetConfig = ci;
                        break;
                    }
                }
            }
            if (targetConfig == null) {
                return Collections.emptyList();
            }
            List<String> layers = new ArrayList<>();
            for (ConfigId layer : session.getState().getContainer().getLayers()) {
                if (layer.getModel().equals(config.getModel()) && targetConfig.getlayers().contains(layer)) {
                    layers.add(layer.getName());
                }
            }
            return layers;
        }

    }

    @Override
    protected void runCommand(ProvisioningSession session, State state, StateExcludeLayersCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            state.excludeLayersConfiguration(session, getConfiguration(state, command), command.getLayers().split(",+"));
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.excludeFailed(), ex);
        }
    }
}
