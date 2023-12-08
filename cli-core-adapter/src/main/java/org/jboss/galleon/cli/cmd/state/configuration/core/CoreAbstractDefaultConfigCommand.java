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
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.AbstractDefaultConfigCommand;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractDefaultConfigCommand<T extends AbstractDefaultConfigCommand> extends CoreAbstractFPProvisionedCommand<T> {

    public static class TargetedFPContentCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

        @Override
        public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
            State session = context.getState();
            List<String> lst = new ArrayList<>();
            if (session != null) {
                AbstractDefaultConfigCommand cmd = (AbstractDefaultConfigCommand) invoc.getCommand();
                String config = cmd.getConfiguration();
                for (Map.Entry<String, FeatureContainer> fp : session.getContainer().getFullDependencies().entrySet()) {
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

    public static class AllConfigsContainer extends FeatureContainer {

        private final FeatureContainer container;

        AllConfigsContainer(FeatureContainer container) {
            super(null, null, container.getProvisioningConfig());
            this.container = container;
        }

        @Override
        public Map<String, List<ConfigInfo>> getFinalConfigs() {
            Map<String, List<ConfigInfo>> map = new HashMap<>();
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

    @Override
    public FeaturePackLocation.ProducerSpec getProducer(ProvisioningSession session, T command) throws CommandExecutionException {
        if (command.getOrigin() == null) {
            return null;
        }
        try {
            return session.getResolvedLocation(null, command.getOrigin()).getProducer();
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.retrieveProducerFailed(), ex);
        }
    }
}
