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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.configuration.AbstractProvisionedDefaultConfigCommand;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractProvisionedDefaultConfigCommand<T extends AbstractProvisionedDefaultConfigCommand> extends CoreAbstractFPProvisionedCommand<T> {

    public static class TargetedFPContentCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

        @Override
        public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
            try {
                State session = context.getState();
                List<String> lst = new ArrayList<>();
                if (session != null) {
                    AbstractProvisionedDefaultConfigCommand cmd = (AbstractProvisionedDefaultConfigCommand) invoc.getCommand();
                    String config = cmd.getConfiguration();
                    for (FeaturePackConfig fc : session.getConfig().getFeaturePackDeps()) {
                        String[] split = config.split("" + PathParser.PATH_SEPARATOR);
                        String model = split[0];
                        String name = split[1];
                        ConfigId cid = new ConfigId(model, name);
                        Collection<ConfigId> configList = cmd.isIncludedConfigs() ? fc.getIncludedConfigs() : fc.getExcludedConfigs();
                        if (configList.contains(cid)) {
                            lst.add(Identity.buildOrigin(fc.getLocation().getProducer()));
                        }
                    }
                }
                return lst;
            } catch (Exception ex) {
                CliLogging.completionException(ex);
                return Collections.emptyList();
            }
        }

    }

    @Override
    public ProducerSpec getProducer(ProvisioningSession session, T command) throws CommandExecutionException {
        return getProducerSpec(session, command);
    }

    public static ProducerSpec getProducerSpec(ProvisioningSession session, AbstractProvisionedDefaultConfigCommand command) throws CommandExecutionException {
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
