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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.maingrp.UninstallCommand;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.PathsUtils;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreUninstallCommand implements GalleonCoreDynamicExecution<ProvisioningSession, UninstallCommand> {

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, UninstallCommand cmd) throws Exception {
        String fpid = cmd.getFPID();
        Path dir = cmd.getAbsolutePath(cmd.getUninstallDir(), session.getPmSession().getAeshContext());
        // Build layout from this directory.
        ProvisioningConfig config = ProvisioningXmlParser.parse(PathsUtils.getProvisioningXml(dir));
        if (config != null) {
            // Silent resolution.
            session.unregisterTrackers();
            try {
                try (ProvisioningLayout<FeaturePackLayout> layout = session.
                        getLayoutFactory().newConfigLayout(config)) {
                    layout.uninstall(session.
                            getResolvedLocation(cmd.getInstallationDirectory(session.getPmSession().getAeshContext()), fpid).getFPID());
                    Set<ProvisioningOption> opts = PluginResolver.newResolver(session,
                            layout).resolve().getInstall();
                    List<AbstractDynamicCommand.DynamicOption> options = new ArrayList<>();
                    for (ProvisioningOption opt : opts) {
                        AbstractDynamicCommand.DynamicOption dynOption = new AbstractDynamicCommand.DynamicOption(opt.getName(),
                                opt.isRequired());
                        options.add(dynOption);
                    }
                    return options;
                }
            } finally {
                session.registerTrackers();
            }
        } else {
            return Collections.emptyList();
        }
    }

    protected ProvisioningManager getManager(ProvisioningSession session, UninstallCommand command) throws ProvisioningException, IOException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), command.isVerbose());
    }

    @Override
    public void execute(ProvisioningSession session, UninstallCommand command, Map<String, String> options) throws CommandExecutionException {

        try {
            getManager(session, command).uninstall(getFPID(session, command), options);
        } catch (ProvisioningException | IOException e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.uninstallFailed(), e);
        }
    }

    private FeaturePackLocation.FPID getFPID(ProvisioningSession session, UninstallCommand command) throws CommandExecutionException {
        String fpid = command.getFPID();
        if (fpid == null) {
            throw new CommandExecutionException("No feature-pack provided");
        }
        try {
            return session.getResolvedLocation(command.
                    getInstallationDirectory(session.getCommandInvocation().getConfiguration().getAeshContext()),
                    fpid).getFPID();
        } catch (Exception e) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.resolveLocationFailed(), e);
        }
    }
}
