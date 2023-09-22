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
package org.jboss.galleon.cli.cmd.featurepack.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.featurepack.GetInfoCommand;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.ALL;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.LAYERS;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.OPTIONAL_PACKAGES;
import org.jboss.galleon.cli.cmd.state.core.StateInfoUtil;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.runtime.ProvisioningRuntimeBuilder;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreGetInfoCommand implements GalleonCoreExecution<ProvisioningSession, GetInfoCommand> {

    public static final String PATCH_FOR = "Patch for ";

    @Override
    public void execute(ProvisioningSession context, GetInfoCommand command) throws CommandExecutionException {
        File file = command.getFile();
        String fpl = command.getFpl();
        if (fpl != null && file != null) {
            throw new CommandExecutionException("File and location can't be both set");
        }
        if (fpl == null && file == null) {
            throw new CommandExecutionException("File or location must be set");
        }
        FeaturePackLayout product = null;
        List<FeaturePackLocation> dependencies = new ArrayList<>();
        ProvisioningConfig provisioning;
        ProvisioningLayout<FeaturePackLayout> layout = null;
        try {
            try {
                if (fpl != null) {
                    FeaturePackLocation loc;
                    loc = context.getResolvedLocation(null, fpl);
                    FeaturePackConfig config = FeaturePackConfig.forLocation(loc);
                    provisioning = ProvisioningConfig.builder().addFeaturePackDep(config).build();
                    layout = context.getLayoutFactory().newConfigLayout(provisioning);
                } else {
                    layout = context.getLayoutFactory().newConfigLayout(file.toPath(), true);
                }

                for (FeaturePackLayout fpLayout : layout.getOrderedFeaturePacks()) {
                    boolean isProduct = true;
                    for (FeaturePackLayout fpLayout2 : layout.getOrderedFeaturePacks()) {
                        if (fpLayout2.getSpec().hasTransitiveDep(fpLayout.getFPID().getProducer())
                                || fpLayout2.getSpec().getFeaturePackDep(fpLayout.getFPID().getProducer()) != null) {
                            isProduct = false;
                            break;
                        }
                    }
                    if (isProduct) {
                        product = fpLayout;
                    } else {
                        dependencies.add(context.getExposedLocation(null, fpLayout.getFPID().getLocation()));
                    }
                }
            } catch (ProvisioningException ex) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.infoFailed(), ex);
            }

            if (product == null) {
                throw new CommandExecutionException("No feature-pack found");
            }
            context.getCommandInvocation().println("");
            StateInfoUtil.printFeaturePack(context, context.getCommandInvocation(),
                    context.getExposedLocation(null, product.getFPID().getLocation()));

            try {
                final FPID patchFor = product.getSpec().getPatchFor();
                if (patchFor != null) {
                    context.getCommandInvocation().println("");
                    context.getCommandInvocation().println(PATCH_FOR + patchFor);
                }
            } catch (ProvisioningException e) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.infoFailed(), e);
            }

            try {
                if (command.getType() != null) {
                    context.getCommandInvocation().println("");
                    switch (command.getType()) {
                        case ALL: {
                            if (displayDependencies(context, dependencies)) {
                                context.getCommandInvocation().println("");
                            }
                            if (displayConfigs(context, layout)) {
                                context.getCommandInvocation().println("");
                            }
                            if (displayLayers(context, layout)) {
                                context.getCommandInvocation().println("");
                            }
                            if (displayOptionalPackages(context, layout)) {
                                context.getCommandInvocation().println("");
                            }
                            displayOptions(context, layout);
                            break;
                        }
                        case FeatureContainerPathConsumer.CONFIGS: {
                            if (!displayConfigs(context, layout)) {
                                context.getCommandInvocation().println(StateInfoUtil.NO_CONFIGURATIONS);
                            }
                            break;
                        }
                        case FeatureContainerPathConsumer.DEPENDENCIES: {
                            if (!displayDependencies(context, dependencies)) {
                                context.getCommandInvocation().println(StateInfoUtil.NO_DEPENDENCIES);
                            }
                            break;
                        }
                        case LAYERS: {
                            if (!displayLayers(context, layout)) {
                                context.getCommandInvocation().println(StateInfoUtil.NO_LAYERS);
                            }
                            break;
                        }
                        case FeatureContainerPathConsumer.OPTIONS: {
                            if (!displayOptions(context, layout)) {
                                context.getCommandInvocation().println(StateInfoUtil.NO_OPTIONS);
                            }
                            break;
                        }
                        case OPTIONAL_PACKAGES: {
                            if (!displayOptionalPackages(context, layout)) {
                                context.getCommandInvocation().println(StateInfoUtil.NO_OPTIONAL_PACKAGES);
                            }
                            break;
                        }
                        default: {
                            throw new CommandExecutionException(CliErrors.invalidInfoType());
                        }
                    }
                }
            } catch (ProvisioningException | IOException ex) {
                throw new CommandExecutionException(context.getPmSession(), CliErrors.infoFailed(), ex);
            }
        } finally {
            if (layout != null) {
                layout.close();
            }
        }
    }

    private boolean displayDependencies(ProvisioningSession session, List<FeaturePackLocation> dependencies) throws CommandExecutionException {
        String str = StateInfoUtil.buildDependencies(dependencies, null);
        if (str != null) {
            session.getCommandInvocation().print(str);
        }
        return str != null;
    }

    private boolean displayConfigs(ProvisioningSession session,
            ProvisioningLayout<FeaturePackLayout> pLayout) throws ProvisioningException, IOException {
        Map<String, List<ConfigInfo>> configs = new HashMap<>();
        try (ProvisioningRuntime rt = ProvisioningRuntimeBuilder.
                newInstance(session.getPmSession().getMessageWriter(false))
                .initRtLayout(pLayout.transform(ProvisioningRuntimeBuilder.FP_RT_FACTORY))
                .build()) {
            for (ProvisionedConfig m : rt.getConfigs()) {
                String model = m.getModel();
                List<ConfigInfo> names = configs.get(model);
                if (names == null) {
                    names = new ArrayList<>();
                    configs.put(model, names);
                }
                if (m.getName() != null) {
                    names.add(new ConfigInfo(model, m.getName(), m.getLayers()));
                }
            }
            String str = StateInfoUtil.buildConfigs(configs, pLayout);
            if (str != null) {
                session.getCommandInvocation().print(str);
            }
            return str != null;
        }
    }

    private boolean displayLayers(ProvisioningSession session,
            ProvisioningLayout<FeaturePackLayout> pLayout) throws ProvisioningException, IOException {
        String str = StateInfoUtil.buildLayers(pLayout);
        if (str != null) {
            session.getCommandInvocation().print(str);
        }
        return str != null;
    }

    private boolean displayOptions(ProvisioningSession session,
            ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        String str = StateInfoUtil.buildOptions(PluginResolver.resolvePlugins(layout));
        if (str != null) {
            session.getCommandInvocation().print(str);
        }
        return str != null;
    }

    private boolean displayOptionalPackages(ProvisioningSession session,
            ProvisioningLayout<FeaturePackLayout> pLayout) throws ProvisioningException, IOException {
        try (ProvisioningRuntime rt = ProvisioningRuntimeBuilder.
                newInstance(session.getPmSession().getMessageWriter(false))
                .initRtLayout(pLayout.transform(ProvisioningRuntimeBuilder.FP_RT_FACTORY))
                .build()) {
            FeatureContainer container = FeatureContainers.
                    fromProvisioningRuntime(session, rt);
            String str = StateInfoUtil.buildOptionalPackages(session.getPmSession(),
                    container, pLayout);
            if (str != null) {
                session.getCommandInvocation().print(str);
            }
            return str != null;
        }
    }
}
