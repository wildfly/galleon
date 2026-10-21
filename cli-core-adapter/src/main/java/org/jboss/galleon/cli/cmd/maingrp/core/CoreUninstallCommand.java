/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
