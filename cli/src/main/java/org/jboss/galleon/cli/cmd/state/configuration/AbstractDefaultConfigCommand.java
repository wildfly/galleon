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
package org.jboss.galleon.cli.cmd.state.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractPathCompleter;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractDefaultConfigCommand extends AbstractFPProvisionedCommand {

    private static class AllConfigsContainer extends FeatureContainer {

        private final FeatureContainer container;

        AllConfigsContainer(FeatureContainer container) {
            super(null, null);
            this.container = container;
        }

        @Override
        public Map<String, List<ConfigInfo>> getFinalConfigs() {
            Map<String, List<ConfigInfo>> map = new HashMap();
            for (FeatureContainer dep : container.getFullDependencies().values()) {
                for (String model : dep.getFinalConfigs().keySet()) {
                    List<ConfigInfo> lst = map.get(model);
                    if (lst == null) {
                        lst = new ArrayList<>();
                        map.put(model, lst);
                    }
                    List<ConfigInfo> configs = dep.getFinalConfigs().get(model);
                    for (ConfigInfo ci : configs) {
                        if (!lst.contains(ci)) {
                            lst.add(ci);
                        }
                    }
                }
            }
            return map;
        }

    }

    public static class ConfigCompleter extends AbstractPathCompleter {

        @Override
        protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation) throws Exception {
            PmSession session = completerInvocation.getPmSession();
            return new AllConfigsContainer(session.getState().getContainer());
        }

        @Override
        protected String getCurrentPath(PmCompleterInvocation session) {
            return ConfigurationUtil.getCurrentPath(session.getPmSession());
        }

        @Override
        protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
            ConfigurationUtil.filterCandidates(consumer, candidates);
        }

    }

    public static class TargetedFPCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            State session = completerInvocation.getPmSession().getState();
            List<String> lst = new ArrayList<>();
            if (session != null) {
                AbstractDefaultConfigCommand cmd = (AbstractDefaultConfigCommand) completerInvocation.getCommand();
                String config = cmd.getConfiguration();
                for (Entry<String, FeatureContainer> fp : session.getContainer().getFullDependencies().entrySet()) {
                    String[] split = config.split("" + PathParser.PATH_SEPARATOR);
                    String model = split[0];
                    String name = split[1];
                    FeatureContainer container = fp.getValue();
                    List<ConfigInfo> confs = container.getFinalConfigs().get(model);
                    for (ConfigInfo ci : confs) {
                        if (ci.getName().equals(name)) {
                            lst.add(fp.getKey());
                            break;
                        }
                    }
                }
            }
            return lst;
        }

    }

    @Argument(required = true, description = "Configuration name",
            completer = ConfigCompleter.class)
    private String configuration;

    @Option(completer = TargetedFPCompleter.class, description = "configuration origin")
    protected String origin;

    protected String getConfiguration() {
        return configuration;
    }

    @Override
    public ProducerSpec getProducer(PmSession session) throws CommandExecutionException {
        if (origin == null) {
            return null;
        }
        try {
            return session.getResolvedLocation(origin).getProducer();
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session, CliErrors.retrieveProducerFailed(), ex);
        }
    }

}
