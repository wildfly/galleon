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
package org.jboss.galleon.cli.cmd.state.pkg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractPathCompleter;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
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
public abstract class AbstractPackageCommand extends AbstractFPProvisionedCommand {

    public static class AllPackagesContainer extends FeatureContainer {

        private final FeatureContainer container;

        public AllPackagesContainer(FeatureContainer container) {
            super(null, null);
            this.container = container;
        }

        @Override
        public Map<String, Group> getPackages() {
            Map<String, Group> map = new HashMap();
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

    public static class PackageCompleter extends AbstractPathCompleter {

        @Override
        protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation) throws Exception {
            PmSession session = completerInvocation.getPmSession();
            return new AllPackagesContainer(session.getState().getContainer());
        }

        @Override
        protected String getCurrentPath(PmCompleterInvocation session) throws Exception {
            return FeatureContainerPathConsumer.PACKAGES_PATH;
        }

        @Override
        protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
            // NO-OP.
        }
    }

    @Argument(required = true, description = "Package name",
            completer = PackageCompleter.class)
    private String pkg;

    protected String getPackage() {
        return pkg;
    }

    @Override
    public ArtifactCoords.Ga getGa(PmSession session) throws CommandExecutionException {
        if (pkg == null) {
            throw new CommandExecutionException("No package set.");
        }
        String fullpath = FeatureContainerPathConsumer.PACKAGES_PATH + pkg;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(new AllPackagesContainer(session.getState().getContainer()), false);
        try {
            PathParser.parse(fullpath, consumer);

            Group grp = consumer.getCurrentNode(fullpath);
            if (grp == null) {
                throw new CommandExecutionException("Invalid package " + pkg);
            }
            PackageInfo info = grp.getPackage();
            if (info == null) {
                throw new CommandExecutionException("Invalid package " + pkg);
            }
            return info.getGav().toGa();
        } catch (PathParserException | PathConsumerException ex) {
            throw new CommandExecutionException(ex);
        }
    }

    @Override
    public FeaturePackConfig getProvisionedFP(PmSession session) throws CommandExecutionException {
        FeaturePackConfig config = super.getProvisionedFP(session);
        if (config == null) {
            // Problem, the package is not directly part of the added FP. Must retrieve it in the packages of
            // its internal dependencies.
            int i = getPackage().indexOf("/");
            String orig = getPackage().substring(0, i);
            String name = getPackage().substring(i + 1);
            ArtifactCoords.Gav gav = null;
            for (Entry<String, FeatureContainer> entry : session.getContainer().getFullDependencies().entrySet()) {
                FeatureContainer container = entry.getValue();
                if (container.getAllPackages().containsKey(Identity.fromString(orig, name))) {
                    gav = container.getGav();
                    break;
                }
            }
            if (gav == null) {
                throw new CommandExecutionException("No package found for " + getPackage());
            }
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                if (c.getGav().equals(gav)) {
                    config = c;
                    break;
                }
            }
        }
        if (config == null) {
            throw new CommandExecutionException("No feature pack found for " + getPackage());
        }
        return config;
    }
}
