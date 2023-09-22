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
package org.jboss.galleon.cli.cmd.state.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateProvisionCommand;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.layout.FeaturePackPluginVisitor;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 * Dual command, applies in case of edit mode or not.
 *
 * @author jdenise@redhat.com
 */
public class CoreStateProvisionCommand implements GalleonCoreDynamicExecution<ProvisioningSession, StateProvisionCommand> {

    protected Set<ProvisioningOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException {
        Set<ProvisioningOption> pluginOptions = new HashSet<>(ProvisioningOption.getStandardList());
        FeaturePackPluginVisitor<InstallPlugin> visitor = new FeaturePackPluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                pluginOptions.addAll(plugin.getOptions().values());
            }
        };
        runtime.getLayout().visitPlugins(visitor, InstallPlugin.class);
        return pluginOptions;
    }

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, StateProvisionCommand cmd) throws Exception {
        State state = session.getState();
        if (state == null) {
            return Collections.emptyList();
        }
        List<AbstractDynamicCommand.DynamicOption> options = new ArrayList<>();
        ProvisioningRuntime rt = state.getRuntime();
        Set<ProvisioningOption> opts = getPluginOptions(rt);
        for (ProvisioningOption opt : opts) {
            AbstractDynamicCommand.DynamicOption dynOption = new AbstractDynamicCommand.DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }

    protected ProvisioningManager getManager(ProvisioningSession context, StateProvisionCommand command) throws ProvisioningException, IOException {
        return context.newProvisioningManager(command.getAbsolutePath(command.getDir(), context.getPmSession().getAeshContext()), command.isVerbose());
    }

    @Override
    public void execute(ProvisioningSession context, StateProvisionCommand command, Map<String, String> options) throws CommandExecutionException {
        State state = context.getState();
        try {
            getManager(context, command).provision(state.getConfig(), options);
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.provisioningFailed(), ex);
        }

        Path home = command.getInstallationDirectory(context.getPmSession().getAeshContext());
        if (home != null && Files.exists(home)) {
            try {
                context.getCommandInvocation().println("Installation done in " + home.toFile().getCanonicalPath());
            } catch (IOException ex) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.retrievePath(), ex);
            }
        } else {
            context.getCommandInvocation().println("Nothing to install");
        }
    }
}
