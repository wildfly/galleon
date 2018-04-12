/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.state;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.aesh.command.CommandDefinition;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Option;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.AbstractPathCompleter;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.PackageInfo;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.runtime.ResolvedSpecId;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "search", description = "search the state for the provided content", activator = StateCommandActivator.class)
public class StateSearchCommand extends PmSessionCommand {
    public static class QueryActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck("package");
            return opt == null || opt.value() == null;
        }
    }

    public static class PackageActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck("query");
            return opt == null || opt.value() == null;
        }
    }

    public static class PackagesCompleter extends AbstractPathCompleter {

        @Override
        protected String getCurrentPath(PmCompleterInvocation session) throws Exception {
            return FeatureContainerPathConsumer.PACKAGES_PATH;
        }

        @Override
        protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
            // NO-OP.
        }

        @Override
        protected FeatureContainer getContainer(PmCompleterInvocation completerInvocation) throws Exception {
            return completerInvocation.getPmSession().getContainer();
        }
    }
    @Option(required = false, activator = QueryActivator.class)
    private String query;

    @Option(required = false, name = "package", completer = PackagesCompleter.class, activator = PackageActivator.class)
    private String pkg;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            FeatureContainer container = invoc.getPmSession().getContainer();

            if (pkg != null) {
                PackageInfo spec = getPackage(invoc.getPmSession().getState(), pkg);
                Set<ResolvedSpecId> fspecs = findFeatures(spec, container);
                invoc.println("Reachable from features:");
                for (ResolvedSpecId id : fspecs) {
                    List<FeatureInfo> features = container.getAllFeatures().get(id);
                    // Can be null if we have all specs whatever the set of features.
                    if (features != null) {
                        for (FeatureInfo fi : features) {
                            invoc.println("  " + fi.getPath());
                        }
                    } else {
                        invoc.println("      [spec only] " + toPath(id));
                    }
                }
                return;
            }
            invoc.println("In packages:");
            StringBuilder pBuilder = new StringBuilder();
            for (Entry<String, Group> pkgs : container.getPackages().entrySet()) {
                Group root = pkgs.getValue();
                for (Group g : root.getGroups()) {
                    PackageInfo p = g.getPackage();
                    if (p.getIdentity().toString().contains(query)) {
                        pBuilder.append("  " + FeatureContainerPathConsumer.PACKAGES_PATH + p.getIdentity()).append(Config.getLineSeparator());
                        pBuilder.append("    Reachable from features:").append(Config.getLineSeparator());
                        Set<ResolvedSpecId> fspecs = findFeatures(p, container);
                        for (ResolvedSpecId id : fspecs) {
                            List<FeatureInfo> features = container.getAllFeatures().get(id);
                            // Can be null if we have all specs whatever the set of features.
                            if (features != null) {
                                for (FeatureInfo fi : features) {
                                    pBuilder.append("      " + fi.getPath()).append(Config.getLineSeparator());
                                }
                            } else {
                                pBuilder.append("      [spec only] " + toPath(id)).append(Config.getLineSeparator());
                            }
                        }
                    }
                }
            }
            if (pBuilder.length() != 0) {
                invoc.println(pBuilder.toString());
            } else {
                invoc.println("Not found in any packages.");
            }
            pBuilder = new StringBuilder();
            // Features?
            invoc.println("In features:");
            for (Entry<ResolvedSpecId, List<FeatureInfo>> features : container.getAllFeatures().entrySet()) {
                ResolvedSpecId id = features.getKey();
                List<FeatureInfo> fs = features.getValue();
                if (fs == null) {
                    if (id.getName().contains(query)) {
                        pBuilder.append("  [spec only] " + toPath(id)).append(Config.getLineSeparator());
                    }
                } else {
                    for (FeatureInfo fi : fs) {
                        if (fi.getPath().contains(query)) {
                            pBuilder.append("  " + fi.getPath()).append(Config.getLineSeparator());
                        }
                    }
                }
            }
            if (pBuilder.length() != 0) {
                invoc.println(pBuilder.toString());
            } else {
                invoc.println("Not found in any feature or feature-spec names.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CommandExecutionException(ex);
        }
    }

    private String toPath(ResolvedSpecId id) {
        return FeatureContainerPathConsumer.FEATURES_PATH
                + Identity.buildOrigin(id.getGav()) + PathParser.PATH_SEPARATOR
                + id.getName().replaceAll("\\.", "" + PathParser.PATH_SEPARATOR);
    }

    private Set<ResolvedSpecId> findFeatures(PackageInfo spec, FeatureContainer container) {
        Set<ResolvedSpecId> fspecs = new HashSet<>();
        for (Entry<ResolvedSpecId, FeatureSpecInfo> features : container.getAllSpecs().entrySet()) {
            for (PackageInfo info : features.getValue().getModules()) {
                Group grp = container.getAllPackages().get(info.getIdentity());
                Set<Identity> identities = new HashSet<>();
                visitPkg(grp, identities);
                if (identities.contains(spec.getIdentity())) {
                    fspecs.add(features.getKey());
                    break;
                }
            }
        }
        return fspecs;
    }

    private PackageInfo getPackage(State state, String id) throws PathParserException, PathConsumerException, ProvisioningException {
        String path = FeatureContainerPathConsumer.PACKAGES_PATH + id;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(state.getContainer(), false);
        PathParser.parse(path, consumer);
        Group grp = consumer.getCurrentNode(path);
        if (grp == null) {
            throw new ProvisioningException("Invalid path");
        }
        if (grp.getPackage() == null) {
            throw new ProvisioningException("Path is not a package");
        }
        return grp.getPackage();
    }

    private void visitPkg(Group pkg, Set<Identity> identities) {
        if (!identities.contains(pkg.getIdentity())) {
            identities.add(pkg.getIdentity());
            for (Group dep : pkg.getGroups()) {
                visitPkg(dep, identities);
            }
        }
    }
}
