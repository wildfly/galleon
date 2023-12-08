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
package org.jboss.galleon.cli.cmd.state.pkg.core;

import java.util.HashMap;
import java.util.Map;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractPackageCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.PackageInfo;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractPackageCommand <T extends AbstractPackageCommand> extends CoreAbstractFPProvisionedCommand<T> {

    public static class AllPackagesContainer extends FeatureContainer {

        private final FeatureContainer container;

        public AllPackagesContainer(FeatureContainer container) {
            super(null, null, container.getProvisioningConfig());
            this.container = container;
        }

        @Override
        public Map<String, Group> getPackages() {
            Map<String, Group> map = new HashMap<>();
            for (FeatureContainer dep : container.getFullDependencies().values()) {
                for (String orig : dep.getPackages().keySet()) {
                    Group root = map.get(orig);
                    if (root == null) {
                        root = Group.fromString(null, orig);
                        map.put(orig, root);
                    }
                    Group depRoot = dep.getPackages().get(orig);
                    for (Group pkg : depRoot.getGroups()) {
                        root.addGroup(pkg);
                    }
                }
            }
            return map;
        }

    }

    @Override
    public ProducerSpec getProducer(ProvisioningSession session, T command) throws CommandExecutionException {
        if (command.getPackage() == null) {
            throw new CommandExecutionException("No package set.");
        }
        String fullpath = FeatureContainerPathConsumer.PACKAGES_PATH + command.getPackage();
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(new AllPackagesContainer(session.getState().getContainer()), false);
        try {
            PathParser.parse(fullpath, consumer);

            Group grp = consumer.getCurrentNode(fullpath);
            if (grp == null) {
                throw new CommandExecutionException("Invalid package " + command.getPackage());
            }
            PackageInfo info = grp.getPackage();
            if (info == null) {
                throw new CommandExecutionException("Invalid package " + command.getPackage());
            }
            return info.getFPID().getProducer();
        } catch (PathParserException | PathConsumerException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.retrieveProducerFailed(), ex);
        }
    }

    @Override
    public FeaturePackConfig getProvisionedFP(ProvisioningSession session, T command) throws CommandExecutionException {
        FeaturePackConfig config = super.getProvisionedFP(session, command);
        if (config == null) {
            // Problem, the package is not directly part of the added FP. Must retrieve it in the packages of
            // its internal dependencies.
            int i = command.getPackage().indexOf("/");
            String orig = command.getPackage().substring(0, i);
            String name = command.getPackage().substring(i + 1);
            FeaturePackLocation.FPID fpid = null;
            FeatureContainer container = session.getContainer().getFullDependencies().get(orig);
            if (container != null) {
                if (container.getAllPackages().containsKey(Identity.fromString(orig, name))) {
                    fpid = container.getFPID();
                }
            }
            if (fpid == null) {
                throw new CommandExecutionException("No package found for " + command.getPackage());
            }
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                if (c.getLocation().getFPID().equals(fpid)) {
                    config = c;
                    break;
                }
            }
            if (config == null) {
                // reset buildID
                FeaturePackLocation noBuildLocation = new FeaturePackLocation(fpid.getUniverse(), fpid.getProducer().getName(),
                        null, null, null);
                for (FeaturePackConfig c : session.getState().getConfig().getTransitiveDeps()) {
                    if (c.getLocation().equals(c.getLocation().hasBuild() ? fpid.getLocation() : noBuildLocation)) {
                        config = c;
                        break;
                    }
                }
            }
        }
        return config;
    }
}
