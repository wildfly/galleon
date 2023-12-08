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
package org.jboss.galleon.cli.cmd.state.feature.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand.DynamicOption;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.feature.StateAddFeatureCommand;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.spec.FeatureParameterSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateAddFeatureCommand implements GalleonCoreDynamicExecution<ProvisioningSession, StateAddFeatureCommand> {

    public static class AllFeaturesContainer extends FeatureContainer {

        private final FeatureContainer container;

        AllFeaturesContainer(FeatureContainer container) {
            super(null, null, container.getProvisioningConfig());
            this.container = container;
        }

        @Override
        public Map<String, Group> getFeatureSpecs() {
            Map<String, Group> map = new HashMap<>();
            for (FeatureContainer dep : container.getFullDependencies().values()) {
                for (String orig : dep.getFeatureSpecs().keySet()) {
                    Group root = map.get(orig);
                    if (root == null) {
                        root = Group.fromString(null, orig);
                        map.put(orig, root);
                    }
                    Group depRoot = dep.getFeatureSpecs().get(orig);
                    for (Group pkg : depRoot.getGroups()) {
                        root.addGroup(pkg);
                    }
                }
            }
            return map;
        }
    }

    @Override
    public void execute(ProvisioningSession context, StateAddFeatureCommand command, Map<String, String> options) throws CommandExecutionException {
        try {
            context.getState().addFeature(context,
                    getSpec(context.getState(),
                            getId(command)), getConfiguration(context.getState(), command), options);
        } catch (IOException | PathParserException | PathConsumerException | ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.addFeatureFailed(), ex);
        }
    }

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, StateAddFeatureCommand cmd) throws Exception {
        if (session.getState() == null) {
            return Collections.emptyList();
        }
        List<DynamicOption> options = new ArrayList<>();
        for (Entry<String, FeatureParameterSpec> entry : getSpec(session.getState(), getId(cmd)).getSpec().getParams().entrySet()) {
            DynamicOption dyn = new DynamicOption(entry.getKey(), !entry.getValue().isNillable() && !entry.getValue().hasDefaultValue());
            String defValue = entry.getValue().getDefaultValue();
            if (defValue != null) {
                dyn.setDefaultValue(defValue.toString());
            }
            options.add(dyn);

        }
        return options;
    }

    private ConfigInfo getConfiguration(State state, StateAddFeatureCommand command) throws PathParserException, PathConsumerException, ProvisioningException {
        @SuppressWarnings("unchecked")
        List<String> args = command.getArgument();
        String config = args.get(0);
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + config + PathParser.PATH_SEPARATOR;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        ConfigInfo ci = consumer.getConfig();
        if (ci == null) {
            throw new ProvisioningException("Not a valid config " + config);
        }
        return ci;
    }

    private FeatureSpecInfo getSpec(State state, String id) throws PathParserException, PathConsumerException, ProvisioningException {
        String path = FeatureContainerPathConsumer.FEATURES_PATH + id;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(new AllFeaturesContainer(state.getContainer()), false);
        PathParser.parse(path, consumer);
        Group grp = consumer.getCurrentNode(path);
        if (grp == null) {
            throw new ProvisioningException("Invalid path");
        }
        if (grp.getSpec() == null) {
            throw new ProvisioningException("Path is not a feature-spec");
        }
        return grp.getSpec();
    }

    private String getId(StateAddFeatureCommand command) {
        @SuppressWarnings("unchecked")
        List<String> args = command.getArgument();
        if (args == null) {
            // Check in argument, that is the option completion case.
            // The value has not yet been injected so is not converted to List<String>
            args = command.getArgumentsValues();
            if (args == null) {
                return null;
            }
        }
        String featureSpec = null;
        if (args.size() > 1) {
            featureSpec = args.get(1);
        }
        return featureSpec;
    }
}
