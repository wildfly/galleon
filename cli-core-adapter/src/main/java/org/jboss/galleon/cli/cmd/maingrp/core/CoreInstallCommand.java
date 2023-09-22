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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.maingrp.InstallCommand;
import org.jboss.galleon.cli.cmd.plugin.core.AbstractPluginsCommand;
import org.jboss.galleon.cli.cmd.state.core.StateInfoUtil;
import org.jboss.galleon.cli.core.LayersConfigBuilder;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.layout.FeaturePackDescriber;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreInstallCommand extends AbstractPluginsCommand<InstallCommand> {

    @Override
    public void runCommand(ProvisioningSession session, InstallCommand command, Map<String, String> options, FeaturePackLocation loc) throws CommandExecutionException {
        try {
            String filePath = (String) command.getValue(InstallCommand.FILE_OPTION_NAME);
            final ProvisioningManager manager = getManager(session, command);
            String layers = (String) command.getValue(InstallCommand.LAYERS_OPTION_NAME);
            if (filePath != null) {
                Path p = Util.resolvePath(session.getPmSession().getAeshContext(), filePath);
                loc = session.getLayoutFactory().addLocal(p, true);
            }
            if (layers == null) {
                String configurations = (String) command.getValue(InstallCommand.DEFAULT_CONFIGS_OPTION_NAME);
                if (configurations == null) {
                    manager.install(loc, options);
                } else {
                    FeaturePackConfig.Builder fpConfig = FeaturePackConfig.builder(loc).setInheritConfigs(false);
                    for (ConfigId c : parseConfigurations(configurations)) {
                        fpConfig.includeDefaultConfig(c);
                    }
                    manager.install(fpConfig.build(), options);
                }
            } else {
                if (!options.containsKey(Constants.OPTIONAL_PACKAGES)) {
                    options.put(Constants.OPTIONAL_PACKAGES, Constants.PASSIVE_PLUS);
                }
                String configuration = (String) command.getValue(InstallCommand.CONFIG_OPTION_NAME);
                String model = null;
                String config = null;
                if (configuration != null) {
                    List<ConfigId> configs = parseConfigurations(configuration);
                    if (configs.size() > 1) {
                        throw new CommandExecutionException(CliErrors.onlyOneConfigurationWithlayers());
                    }
                    if (!configs.isEmpty()) {
                        ConfigId id = configs.get(0);
                        model = id.getModel();
                        config = id.getName();
                    }
                }
                manager.provision(new LayersConfigBuilder(manager, session, layers.split(",+"),
                        model,
                        config, loc).build(), options);
            }
            session.getPmSession().println("Feature pack installed.");
            if (manager.isRecordState() && !loc.isMavenCoordinates()) {
                if (!loc.hasBuild() || loc.getChannelName() == null) {
                    loc = manager.getProvisioningConfig().getFeaturePackDep(loc.getProducer()).getLocation();
                }
                StateInfoUtil.printFeaturePack(session, session.getCommandInvocation(),
                        session.getExposedLocation(manager.getInstallationHome(), loc));
            }
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.installFailed(), ex);
        }
    }

    private ProvisioningManager getManager(ProvisioningSession session, InstallCommand command) throws ProvisioningException, IOException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), command.isVerbose());
    }

    private static List<ConfigId> parseConfigurations(String configurations) {
        String[] split = configurations.split(",+");
        List<ConfigId> configs = new ArrayList<>();
        if (split.length == 0) {
            return configs;
        }
        for (String c : split) {
            String config = null;
            String model = null;
            c = c.trim();
            if (!c.isEmpty()) {
                int i = c.indexOf("/");
                if (i < 0) {
                    config = c.trim();
                } else {
                    model = c.substring(0, i).trim();
                    if (i < c.length() - 1) {
                        config = c.substring(i + 1, c.length()).trim();
                    }
                }
                configs.add(new ConfigId(model, config));
            }
        }
        return configs;
    }

    @Override
    protected Set<ProvisioningOption> getPluginOptions(ProvisioningSession session, InstallCommand cmd, FeaturePackLocation loc) throws ProvisioningException {
        try {
            //If we have a file, retrieve the options from the file.
            String file = (String) cmd.getValue(InstallCommand.FILE_OPTION_NAME);
            if (file == null) {
                // Check in argument or option, that is the option completion case.
                file = cmd.getOptionValue(InstallCommand.FILE_OPTION_NAME);
            }
            if (file == null) {
                return session.getResolver().get(loc.toString(),
                        PluginResolver.newResolver(session, loc)).getInstall();
            } else {
                return session.getResolver().get(file,
                        PluginResolver.newResolver(session, loc)).getInstall();
            }
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new ProvisioningException(ex);
        } catch (ExecutionException ex) {
            throw new ProvisioningException(ex.getCause());

        }
    }

    @Override
    protected String getId(ProvisioningSession session, InstallCommand cmd) throws CommandExecutionException {
        String filePath = (String) cmd.getValue(InstallCommand.FILE_OPTION_NAME);
        if (filePath == null) {
            filePath = cmd.getOptionValue(InstallCommand.FILE_OPTION_NAME);
            if (filePath == null) {
                return super.getId(session, cmd);
            }
        }
        Path path;
        try {
            path = Util.resolvePath(session.getPmSession().getAeshContext(), filePath);
        } catch (IOException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return FeaturePackDescriber.readSpec(path).getFPID().toString();
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.retrieveFeaturePackID(), ex);
        }
    }
}
