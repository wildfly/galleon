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
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractPackageCommand;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractPackageCommand.PackageCompleter;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.PackageInfo;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.runtime.ResolvedSpecId;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "search", description = HelpDescriptions.SEARCH_STATE)
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

    @Option(required = false, activator = QueryActivator.class, description = HelpDescriptions.SEARCH_QUERY)
    private String query;

    @Option(required = false, name = "package", completer = PackageCompleter.class,
            activator = PackageActivator.class, description = HelpDescriptions.PACKAGE_PATH)
    private String pkg;

    @Option(required = false, name = "include-dependencies", hasValue = false,
            description = HelpDescriptions.SEARCH_IN_DEPENDENCIES)
    private Boolean inDependencies;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            if (query == null && pkg == null) {
                throw new CommandExecutionException("One of --query or --package must be set");
            }
            FeatureContainer container = invoc.getPmSession().getContainer();
            run(invoc.getPmSession().getContainer(), invoc, false);

            if (inDependencies) {
                if (!container.getFullDependencies().isEmpty()) {
                    invoc.println("");
                    invoc.println("Search in dependencies");
                    for (FeatureContainer c : container.getFullDependencies().values()) {
                        invoc.println("dependency: " + c.getFPID());
                        run(c, invoc, true);
                    }
                }
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.searchFailed(), ex);
        }
    }

    private void run(FeatureContainer container, PmCommandInvocation invoc, boolean dependencySearch) throws PathParserException,
            PathConsumerException, ProvisioningException {
        if (pkg != null) {
            PackageInfo spec = getPackage(dependencySearch ? container : new AbstractPackageCommand.AllPackagesContainer(container), pkg);
            invoc.println(Config.getLineSeparator() + "As a direct dependency of a package:");
            StringBuilder pBuilder = new StringBuilder();
            for (Entry<String, Group> pkgs : container.getPackages().entrySet()) {
                Group root = pkgs.getValue();
                for (Group g : root.getGroups()) {
                    for (Group dep : g.getGroups()) {
                        if (dep.getIdentity().equals(spec.getIdentity())) {
                            pBuilder.append("  " + g.getIdentity()).append(Config.getLineSeparator());
                            break;
                        }
                    }
                }
            }
            if (pBuilder.length() != 0) {
                invoc.println(pBuilder.toString());
            } else {
                invoc.println("NONE");
            }
            Set<ResolvedSpecId> fspecs = findFeatures(spec, container);
            invoc.println("Reachable from features:");
            if (fspecs.isEmpty()) {
                invoc.println("NONE");
            } else {
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
            }
            return;
        }
        invoc.println(Config.getLineSeparator() + "Packages:");
        StringBuilder pBuilder = new StringBuilder();
        for (Entry<String, Group> pkgs : container.getPackages().entrySet()) {
            Group root = pkgs.getValue();
            for (Group g : root.getGroups()) {
                PackageInfo p = g.getPackage();
                if (p.getIdentity().toString().contains(query)) {
                    pBuilder.append("  " + FeatureContainerPathConsumer.PACKAGES_PATH + p.getIdentity()).append(Config.getLineSeparator());
                    if (!dependencySearch) {
                        pBuilder.append("    Reachable from features:").append(Config.getLineSeparator());
                        Set<ResolvedSpecId> fspecs = findFeatures(p, container);
                        if (fspecs.isEmpty()) {
                            pBuilder.append("      NONE" + Config.getLineSeparator());
                        }
                        for (ResolvedSpecId id : fspecs) {
                            List<FeatureInfo> features = container.getAllFeatures().get(id);
                            // Can be null if we have all specs whatever the set of features.
                            if (features != null) {
                                for (FeatureInfo fi : features) {
                                    pBuilder.append("      " + fi.getPath()).append(Config.getLineSeparator());
                                }
                            } else {
                                pBuilder.append("  [spec only] " + toPath(id)).append(Config.getLineSeparator());
                            }
                        }
                    }
                }
            }
        }
        if (pBuilder.length() != 0) {
            invoc.println(pBuilder.toString());
        } else {
            invoc.println("NONE");
        }

        pBuilder = new StringBuilder();
        invoc.println(Config.getLineSeparator() + "Package dependencies:");
        for (Entry<String, Group> pkgs : container.getPackages().entrySet()) {
            Group root = pkgs.getValue();
            for (Group g : root.getGroups()) {
                StringBuilder depBuilder = new StringBuilder();
                for (Group dep : g.getGroups()) {
                    if (dep.getIdentity().toString().contains(query)) {
                        depBuilder.append("  " + dep.getIdentity()).append(Config.getLineSeparator());
                        break;
                    }
                }
                if (depBuilder.length() != 0) {
                    pBuilder.append("  Found as a direct dependencies of " + g.getIdentity()).append(Config.getLineSeparator());
                    pBuilder.append(depBuilder);
                }
            }
        }
        if (pBuilder.length() != 0) {
            invoc.println(pBuilder.toString());
        } else {
            invoc.println("NONE");
        }

        pBuilder = new StringBuilder();
        invoc.println(Config.getLineSeparator() + "Package content:");
        for (Entry<String, Group> entry : container.getPackages().entrySet()) {
            Group root = entry.getValue();
            for (Group g : root.getGroups()) {
                PackageInfo pkginfo = g.getPackage();
                StringBuilder contentBuilder = new StringBuilder();
                for (String c : pkginfo.getContent()) {
                    if (c.contains(query)) {
                        contentBuilder.append(c).append(Config.getLineSeparator());
                    }
                }
                if (contentBuilder.length() != 0) {
                    pBuilder.append("  Found in content of "
                            + g.getIdentity()).append(Config.getLineSeparator());
                    pBuilder.append(contentBuilder);
                }
            }
        }
        if (pBuilder.length() != 0) {
            invoc.println(pBuilder.toString());
        } else {
            invoc.println("NONE");
        }
        pBuilder = new StringBuilder();
        // Features?
        invoc.println(Config.getLineSeparator() + "Features:");
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
            invoc.println("NONE");
        }
    }

    private String toPath(ResolvedSpecId id) {
        return FeatureContainerPathConsumer.FEATURES_PATH
                + Identity.buildOrigin(id.getProducer()) + PathParser.PATH_SEPARATOR
                + id.getName().replaceAll("\\.", "" + PathParser.PATH_SEPARATOR);
    }

    private Set<ResolvedSpecId> findFeatures(PackageInfo spec, FeatureContainer container) {
        Set<ResolvedSpecId> fspecs = new HashSet<>();
        for (Entry<ResolvedSpecId, FeatureSpecInfo> features : container.getAllSpecs().entrySet()) {
            for (PackageInfo info : features.getValue().getPackages()) {
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

    private PackageInfo getPackage(FeatureContainer container, String id) throws PathParserException, PathConsumerException, ProvisioningException {
        String path = FeatureContainerPathConsumer.PACKAGES_PATH + id;
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(container, false);
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

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
