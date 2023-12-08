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
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.terminal.Key;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand.DynamicOption;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand;
import static org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins.DIR_OPTION_NAME;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.UPDATES_AVAILABLE;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.UP_TO_DATE;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.FP_OPTION_NAME;
import org.jboss.galleon.cli.cmd.maingrp.UpdateCommand;
import org.jboss.galleon.cli.cmd.maingrp.core.CoreCheckUpdatesCommand.Updates;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreUpdateCommand implements GalleonCoreDynamicExecution<ProvisioningSession, UpdateCommand> {

    @Override
    public List<DynamicOption> getDynamicOptions(ProvisioningSession session, UpdateCommand cmd) throws Exception {
        String targetDirArg = (String) cmd.getValue(DIR_OPTION_NAME);
        if (targetDirArg == null) {
            // Check in argument or option, that is the option completion case.
            targetDirArg = cmd.getOptionValue(DIR_OPTION_NAME);
        }
        Path installation = cmd.getAbsolutePath(targetDirArg, session.getPmSession().getAeshContext());
        ProvisioningConfig config = session.newProvisioningManager(installation, false).getProvisioningConfig();
        Set<ProvisioningOption> opts = session.getResolver().get(null, PluginResolver.newResolver(session, config)).getDiff();
        List<DynamicOption> options = new ArrayList<>();
        for (ProvisioningOption opt : opts) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }
    protected ProvisioningManager getManager(ProvisioningSession session, UpdateCommand command) throws ProvisioningException, IOException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), command.isVerbose());
    }
    @Override
    public void execute(ProvisioningSession session, UpdateCommand command, Map<String, String> options) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session, command);
            Updates updates = CoreCheckUpdatesCommand.getUpdatesTable(mgr, session, allDependencies(command), getFP(command));
            if (updates.plan.isEmpty()) {
                session.getPmSession().println(UP_TO_DATE);
            } else {
                session.getPmSession().println(UPDATES_AVAILABLE);
                session.getPmSession().println(updates.t.build());
                if (!noConfirm(command)) {
                    try {
                        Key k = null;
                        while (k == null || (!Key.y.equals(k) && !Key.n.equals(k))) {
                            session.getPmSession().print("Proceed with latest updates [y/n]?");
                            KeyAction a = session.getCommandInvocation().input();
                            k = Key.findStartKey(a.buffer().array());
                        }
                        if (Key.n.equals(k)) {
                            return;
                        }
                    } finally {
                        session.getPmSession().println("");
                    }
                }
                mgr.apply(updates.plan, options);
            }
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(),
                    CliErrors.updateFailed(), ex);
        } catch (InterruptedException ignored) {
            // Just exit the command smoothly
        }
    }

    private boolean noConfirm(UpdateCommand command) {
        return command.contains(UpdateCommand.YES_OPTION_NAME);
    }

    private String getFP(UpdateCommand command) {
        return (String) command.getValue(FP_OPTION_NAME);
    }

    private boolean allDependencies(UpdateCommand command) {
        return command.contains(CheckUpdatesCommand.ALL_DEPENDENCIES_OPTION_NAME);
    }
}
