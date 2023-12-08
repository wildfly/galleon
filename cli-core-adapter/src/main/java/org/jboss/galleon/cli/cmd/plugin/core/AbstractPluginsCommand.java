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
package org.jboss.galleon.cli.cmd.plugin.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import static org.jboss.galleon.cli.cmd.AbstractDynamicCommand.ARGUMENT_NAME;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand.DynamicOption;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 * An abstract command that discover plugin options based on the fp or stream
 * argument.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractPluginsCommand<T extends org.jboss.galleon.cli.cmd.plugin.AbstractPluginsCommand> implements GalleonCoreDynamicExecution<ProvisioningSession, T> {

    @Override
    public void execute(ProvisioningSession session, T command, Map<String, String> options) throws CommandExecutionException {
        if (command.isVerbose()) {
            session.getPmSession().enableMavenTrace(true);
        }
        try {
            final String id = getId(session, command);
            final FeaturePackLocation loc = id == null ? null : session.
                    getResolvedLocation(command.getInstallationDirectory(session.getPmSession().getAeshContext()), FeaturePackLocation.fromString(id));
            runCommand(session, command, options, loc);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.resolveLocationFailed(), ex);
        } finally {
            session.getPmSession().enableMavenTrace(false);
        }
    }

    protected abstract void runCommand(ProvisioningSession session, T command, Map<String, String> options,
            FeaturePackLocation loc) throws CommandExecutionException;

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, T cmd) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        String id = getId(session, cmd);
        FeaturePackLocation fpl = session.getResolvedLocation(cmd.getInstallationDirectory(session.getPmSession().getAeshContext()),
                getId(session, cmd));
        Set<ProvisioningOption> pluginOptions = getPluginOptions(session, cmd, fpl);
        for (ProvisioningOption opt : pluginOptions) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired());
            options.add(dynOption);
        }
        return options;
    }

    protected String getId(ProvisioningSession session, T cmd) throws CommandExecutionException {
        String streamName = (String) cmd.getValue(ARGUMENT_NAME);
        if (streamName == null) {
            // Check in argument or option, that is the option completion case.
            streamName = cmd.getArgumentValue();
        }
        if (streamName != null) {
            try {
                return session.getResolvedLocation(cmd.getInstallationDirectory(session.getPmSession().getAeshContext()),
                        FeaturePackLocation.fromString(streamName)).toString();
            } catch (ProvisioningException ex) {
                // Ok, no id set.
            }
        }
        return null;
    }

    protected abstract Set<ProvisioningOption> getPluginOptions(ProvisioningSession session, T cmd, FeaturePackLocation loc) throws ProvisioningException;
}
