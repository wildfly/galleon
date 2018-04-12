/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.state.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aesh.command.CommandException;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.map.MapCommand;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.AbstractPathCompleter;
import org.jboss.galleon.cli.cmd.state.StateEditCommandActivator;
import org.jboss.galleon.cli.cmd.state.configuration.ProvisionedConfigurationCompleter;
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
public class StateAddFeatureCommand extends AbstractDynamicCommand {

    private static class AllFeaturesContainer extends FeatureContainer {

        private final FeatureContainer container;

        AllFeaturesContainer(FeatureContainer container) {
            super(null, null);
            this.container = container;
        }

        @Override
        public Map<String, Group> getFeatureSpecs() {
            Map<String, Group> map = new HashMap();
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

    public static class FeatureSpecIdCompleter extends AbstractPathCompleter {

        @Override
        protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation) throws Exception {
            PmSession session = completerInvocation.getPmSession();
            return new AllFeaturesContainer(session.getState().getContainer());
        }

        @Override
        protected String getCurrentPath(PmCompleterInvocation session) throws Exception {
            return FeatureContainerPathConsumer.FEATURES_PATH;
        }

        @Override
        protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
            // NO-OP.
        }
    }

    public static class AddArgumentsCompleter implements OptionCompleter<PmCompleterInvocation> {

        @Override
        public void complete(PmCompleterInvocation completerInvocation) {
            MapCommand<PmCommandInvocation> cmd = (MapCommand<PmCommandInvocation>) completerInvocation.getCommand();
            Object value = cmd.getValue(AbstractDynamicCommand.ARGUMENT_NAME);
            if (value == null || !(value instanceof List) || ((List) value).isEmpty()) {
                new ProvisionedConfigurationCompleter().complete(completerInvocation);
            } else if ((value instanceof List) && ((List) value).size() == 1) {
                new FeatureSpecIdCompleter().complete(completerInvocation);
            }
        }

    }

    public StateAddFeatureCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        try {
            session.getPmSession().getState().addFeature(session.getPmSession(),
                    getSpec(session.getPmSession().getState(),
                            getId(session.getPmSession())), getConfiguration(session.getPmSession().getState()), options);
        } catch (IOException | PathParserException | PathConsumerException | ProvisioningException ex) {
            throw new CommandExecutionException(ex);
        }
    }

    private ConfigInfo getConfiguration(State state) throws PathParserException, PathConsumerException, ProvisioningException {
        List<String> args = (List<String>) getValue(ARGUMENT_NAME);
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

    @Override
    protected String getName() {
        return "add";
    }

    @Override
    protected String getDescription() {
        return "Add a new feature";
    }

    @Override
    protected String getId(PmSession session) {
        List<String> args = (List<String>) getValue(ARGUMENT_NAME);
        if (args == null) {
            // Check in argument, that is the option completion case.
            // The value has not yet been injected so is not converted to List<String>
            args = getArgumentsValues();
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

    @Override
    protected void doValidateOptions() throws CommandException {
        List<String> args = (List<String>) getValue(ARGUMENT_NAME);
        if (args != null) {
            if (args.size() == 2) {
                return;
            }
        }
        throw new CommandException("Invalid config and feature-spec");
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state, String id) throws Exception {
        List<DynamicOption> options = new ArrayList<>();
        for (Entry<String, FeatureParameterSpec> entry : getSpec(state, id).getSpec().getParams().entrySet()) {
            DynamicOption dyn = new DynamicOption(entry.getKey(), !entry.getValue().isNillable() && !entry.getValue().hasDefaultValue(), true);
            String defValue = entry.getValue().getDefaultValue();
            if (defValue != null) {
                dyn.setDefaultValue(defValue.toString());
            }
            options.add(dyn);

        }
        return options;
    }

    @Override
    protected List<ProcessedOption> getStaticOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        options.add(ProcessedOptionBuilder.builder().name(ARGUMENT_NAME).
                hasValue(true).
                description("config followed by feature id").
                type(String.class).
                required(true).
                hasMultipleValues(true).
                optionType(OptionType.ARGUMENTS).
                completer(AddArgumentsCompleter.class).
                build());
        return options;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new StateEditCommandActivator();
    }
}
