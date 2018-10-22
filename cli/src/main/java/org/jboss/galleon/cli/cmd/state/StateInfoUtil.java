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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import org.aesh.utils.Config;
import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.cli.cmd.Table.Cell;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.ALL;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.PATCHES;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.PackageInfo;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.CONFIGS;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.DEPENDENCIES;
import static org.jboss.galleon.cli.path.FeatureContainerPathConsumer.OPTIONS;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.cli.resolver.ResolvedPlugins;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.spec.FeatureAnnotation;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class StateInfoUtil {

    public static final String DEFAULT_UNIVERSE = "default";

    public static final String NO_CONFIGURATIONS = "No configurations.";
    public static final String NO_DEPENDENCIES = "No dependencies.";
    public static final String NO_OPTIONS = "No options.";
    public static final String NO_PATCHES = "No patches.";

    public static void printContentPath(PmCommandInvocation session, FeatureContainer fp, String path)
            throws ProvisioningException, PathParserException, PathConsumerException, IOException {
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(fp, false);
        PathParser.parse(path, consumer);
        Group grp = consumer.getCurrentNode(path);
        if (grp != null) { // entered some content
            if (grp.getFeature() != null) {
                displayFeature(session, grp);
            } else if (grp.getSpec() != null) {
                displayFeatureSpec(session, grp);
            } else if (grp.getPackage() != null) {
                displayPackage(session, grp);
            } else if (!grp.getGroups().isEmpty()) {
                displayContainmentGroup(session, grp);
            }
        }
    }

    private static void displayContainmentGroup(PmCommandInvocation session, Group grp) {
        for (Group fg : grp.getGroups()) {
            session.println(fg.getIdentity().getName());
        }
    }

    private static void displayFeature(PmCommandInvocation session, Group grp) throws ProvisioningException {
        // Feature and spec.
        FeatureInfo f = grp.getFeature();
        session.println("");
        session.println("Type       : " + f.getType());
        session.println("Path       : " + f.getPath());
        session.println("Origin     : " + f.getSpecId().getProducer());
        session.println("Description: " + f.getDescription());
        session.println("");
        session.println("Parameters id");
        if (f.getFeatureId() == null) {
            session.println("NONE");
        } else {
            for (Entry<String, String> entry : f.getFeatureId().getParams().entrySet()) {
                session.println(entry.getKey() + "=" + entry.getValue());
            }
        }
        session.println(Config.getLineSeparator() + "Feature XML extract");
        StringBuilder xmlBuilder = new StringBuilder();
        /**
         * <feature spec="core-service.vault">
         * <param name="core-service" value="vault"/>
         * <param name="module" value="aValue"/>
         * <param name="code" value="aValue"/>
         * </feature>
         */
        xmlBuilder.append("<feature spec=\"" + f.getType() + "\">").append(Config.getLineSeparator());
        String tab = "  ";
        for (Entry<String, Object> p : f.getResolvedParams().entrySet()) {
            if (!Constants.GLN_UNDEFINED.equals(p.getValue())) {
                xmlBuilder.append(tab + "<param name=\"" + p.getKey() + "\"" + " value=\"" + p.getValue() + "\"/>").append(Config.getLineSeparator());
            }
        }
        xmlBuilder.append("</feature>").append(Config.getLineSeparator());
        session.println(xmlBuilder.toString());
        session.println("Unset parameters");
        if (f.getUndefinedParams().isEmpty()) {
            session.println("NONE");
        }
        for (String p : f.getUndefinedParams()) {
            session.println(tab + "<param name=\"" + p + "\"" + " value=\"???\"/>");
        }
    }

    private static void displayFeatureSpec(PmCommandInvocation session, Group grp) throws IOException {
        FeatureSpecInfo f = grp.getSpec();
        session.println("");
        session.println("Feature type       : " + f.getSpecId().getName());
        session.println("Feature origin     : " + f.getSpecId().getProducer());
        session.println("Feature description: " + f.getDescription());
        if (!f.isEnabled()) {
            session.println("WARNING! The feature is not enabled.");
            session.println("Missing packages");
            for (Identity m : f.getMissingPackages()) {
                session.println(m.toString());
            }
        }
        List<FeatureParameterSpec> idparams = f.getSpec().getIdParams();
        String tab = "  ";
        session.println(Config.getLineSeparator() + "Feature Id parameters");
        if (idparams.isEmpty()) {
            session.println("NONE");
        } else {
            for (FeatureParameterSpec param : idparams) {
                StringBuilder builder = new StringBuilder();
                builder.append(tab + param.getName()).append(Config.getLineSeparator());
                builder.append(tab + tab + "description  : " + "no description available").append(Config.getLineSeparator());
                builder.append(tab + tab + "type         : " + param.getType()).append(Config.getLineSeparator());
                builder.append(tab + tab + "default-value: " + param.getDefaultValue()).append(Config.getLineSeparator());
                builder.append(tab + tab + "nillable     : " + param.isNillable()).append(Config.getLineSeparator());
                session.print(builder.toString());
            }
        }
        // Add spec parameters
        session.println(Config.getLineSeparator() + "Feature parameters");
        Map<String, FeatureParameterSpec> params = f.getSpec().getParams();
        if (params.isEmpty()) {
            session.println("NONE");
        } else {
            for (Entry<String, FeatureParameterSpec> entry : params.entrySet()) {
                FeatureParameterSpec param = entry.getValue();
                if (!param.isFeatureId()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tab + param.getName()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "description  : " + "no description available").append(Config.getLineSeparator());
                    builder.append(tab + tab + "type         : " + param.getType()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "default-value: " + param.getDefaultValue()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "nillable     : " + param.isNillable()).append(Config.getLineSeparator());
                    session.println(builder.toString());
                }
            }
        }

        session.println(Config.getLineSeparator() + "Packages");
        if (f.getPackages().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (PackageInfo p : f.getPackages()) {
                session.println(p.getIdentity().toString());
            }
        }

        session.println(Config.getLineSeparator() + "Provided capabilities");
        if (f.getSpec().getProvidedCapabilities().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (CapabilitySpec c : f.getSpec().getProvidedCapabilities()) {
                session.println(tab + c.toString());
            }
        }

        session.println(Config.getLineSeparator() + "Consumed capabilities");
        if (f.getSpec().getRequiredCapabilities().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (CapabilitySpec c : f.getSpec().getRequiredCapabilities()) {
                session.println(tab + c.toString());
            }
        }

        session.println(Config.getLineSeparator() + "Features dependencies");
        if (f.getSpec().getFeatureDeps().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureDependencySpec c : f.getSpec().getFeatureDeps()) {
                session.println(tab + c.getFeatureId().toString());
            }
        }

        session.println(Config.getLineSeparator() + "Features references");
        if (f.getSpec().getFeatureRefs().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureReferenceSpec c : f.getSpec().getFeatureRefs()) {
                session.println(tab + c.getFeature());
            }
        }

        session.println(Config.getLineSeparator() + "Features Annotations");
        if (f.getSpec().getAnnotations().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureAnnotation c : f.getSpec().getAnnotations()) {
                session.println(tab + c.toString());
            }
        }
    }

    private static void displayPackage(PmCommandInvocation session, Group grp) throws IOException {
        PackageInfo pkg = grp.getPackage();
        session.println("");
        session.println("Package name : " + pkg.getIdentity().getName());
        session.println("Package origin : " + pkg.getIdentity().getOrigin());

        session.println(Config.getLineSeparator() + "Package providers (features that depend on this package)");
        if (pkg.getProviders().isEmpty()) {
            session.println("default provider");
        } else {
            for (Identity id : pkg.getProviders()) {
                session.println(id.toString());
            }
        }

        session.println(Config.getLineSeparator() + "Package dependencies");
        if (grp.getGroups().isEmpty()) {
            session.println("NONE");
        } else {
            for (Group dep : grp.getGroups()) {
                session.println(dep.getIdentity().toString());
            }
        }
        session.println(Config.getLineSeparator() + "Package content");
        String customContent = pkg.getCustomContent();
        if (customContent != null) {
            session.println(customContent);
        } else if (pkg.getContent().isEmpty()) {
            session.println("NONE");
        } else {
            StringBuilder contentBuilder = new StringBuilder();
            for (String name : pkg.getContent()) {
                contentBuilder.append("  " + name + Config.getLineSeparator());
            }
            session.println(contentBuilder.toString());
        }
    }

    public static String buildConfigs(Map<String, List<ConfigInfo>> configs) {
        if (!configs.isEmpty()) {
            Table table = new Table(Headers.CONFIGURATION, Headers.NAME);
            for (Entry<String, List<ConfigInfo>> entry : configs.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    List<Table.Cell> cells = new ArrayList<>();
                    cells.add(new Table.Cell(entry.getKey()));
                    Table.Cell names = new Table.Cell();
                    cells.add(names);
                    for (ConfigInfo name : entry.getValue()) {
                        names.addLine(name.getName());
                    }
                    table.addCellsLine(cells);
                }
            }
            table.sort(Table.SortType.ASCENDANT);
            return table.build();
        }
        return null;
    }

    public static String buildDependencies(List<FeaturePackLocation> dependencies, Map<FPID, FeaturePackConfig> configs) {
        if (!dependencies.isEmpty()) {
            boolean showPatches = configs == null ? false : showPatches(configs.values());
            List<String> headers = new ArrayList<>();
            headers.add(Headers.DEPENDENCY);
            headers.add(Headers.BUILD);
            if (showPatches) {
                headers.add(Headers.PATCHES);
            }
            headers.add(Headers.UPDATE_CHANNEL);
            Table table = new Table(headers);
            for (FeaturePackLocation d : dependencies) {
                List<Cell> line = new ArrayList<>();
                line.add(new Cell(d.getProducerName()));
                line.add(new Cell(d.getBuild()));
                if (showPatches) {
                    FeaturePackConfig config = configs.get(d.getFPID());
                    if (config != null && config.hasPatches()) {
                        Cell patches = new Cell();
                        for (FPID p : config.getPatches()) {
                            patches.addLine(p.getBuild());
                        }
                        line.add(patches);
                    }
                }
                line.add(new Cell(formatChannel(d)));
                table.addCellsLine(line);
            }
            table.sort(Table.SortType.ASCENDANT);
            return table.build();
        }
        return null;
    }

    public static String buildPatches(PmCommandInvocation invoc, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        if (!layout.hasPatches()) {
            return null;
        }
        Table table = new Table(Headers.PATCH, Headers.PATCH_FOR, Headers.UPDATE_CHANNEL);

        for (FeaturePackLayout fpLayout : layout.getOrderedFeaturePacks()) {
            List<FeaturePackLayout> patches = layout.getPatches(fpLayout.getFPID());
            for (FeaturePackLayout patch : patches) {
                FeaturePackLocation loc = invoc.getPmSession().getExposedLocation(null, patch.getFPID().getLocation());
                FPID patchFor = patch.getSpec().getPatchFor();
                table.addLine(patch.getFPID().getBuild(),
                        patchFor.getProducer().getName() + FeaturePackLocation.BUILD_START + patchFor.getBuild(),
                        formatChannel(loc));
            }
        }
        if (!table.isEmpty()) {
            table.sort(Table.SortType.ASCENDANT);
            return table.build();
        }
        return null;
    }

    public static void printFeaturePack(PmCommandInvocation commandInvocation, FeaturePackLocation loc) {
        loc = commandInvocation.getPmSession().getExposedLocation(null, loc);
        Table t = new Table(Headers.PRODUCT, Headers.BUILD, Headers.UPDATE_CHANNEL);
        t.addLine(loc.getProducer().getName(), loc.getBuild(), formatChannel(loc));
        commandInvocation.print(t.build());
    }

    public static void printFeaturePacks(PmCommandInvocation commandInvocation,
            Path installation, Collection<FeaturePackConfig> fps) {
        boolean showPatches = showPatches(fps);
        List<String> headers = new ArrayList<>();
        headers.add(Headers.PRODUCT);
        headers.add(Headers.BUILD);
        if (showPatches) {
            headers.add(Headers.PATCHES);
        }
        headers.add(Headers.UPDATE_CHANNEL);
        Table t = new Table(headers);
        for (FeaturePackConfig c : fps) {
            FeaturePackLocation loc = commandInvocation.getPmSession().getExposedLocation(installation, c.getLocation());
            List<Cell> line = new ArrayList<>();
            line.add(new Cell(loc.getProducer().getName()));
            line.add(new Cell(loc.getBuild()));
            if (showPatches) {
                if (c.hasPatches()) {
                    Cell patches = new Cell();
                    for (FPID p : c.getPatches()) {
                        patches.addLine(p.getBuild());
                    }
                    line.add(patches);
                }
            }
            line.add(new Cell(formatChannel(loc)));
            t.addCellsLine(line);
        }
        if (!t.isEmpty()) {
            t.sort(Table.SortType.ASCENDANT);
            commandInvocation.print(t.build());
        }
    }

    private static boolean showPatches(Collection<FeaturePackConfig> configs) {
        if (configs != null) {
            for (FeaturePackConfig c : configs) {
                if (c != null && c.hasPatches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String buildOptions(ResolvedPlugins plugins) {
        StringBuilder builder = new StringBuilder();
        boolean found = false;
        if (!plugins.getInstall().isEmpty()) {
            found = true;
            builder.append("Install and provision commands options").append(Config.getLineSeparator());
            builder.append(buildOptionsTable(plugins.getInstall())).append(Config.getLineSeparator());
        }
        if (!plugins.getInstall().isEmpty()) {
            found = true;
            builder.append("Update command options").append(Config.getLineSeparator());
            builder.append(buildOptionsTable(plugins.getDiff()));
        }
        if (found) {
            return builder.toString();
        } else {
            return null;
        }
    }

    public static String formatChannel(FeaturePackLocation loc) {
        String channel = loc.getFrequency() == null ? loc.getChannel().getName() : loc.getChannel().getName()
                + "/" + loc.getFrequency();
        return (loc.getUniverse() == null ? "" : loc.getUniverse() + "@") + channel;
    }

    private static String buildOptionsTable(Set<ProvisioningOption> options) {
        Table t = new Table(Headers.OPTION, Headers.REQUIRED, Headers.DEFAULT_VALUE);
        for (ProvisioningOption opt : options) {
            t.addLine("--" + opt.getName() + (opt.isAcceptsValue() ? "=" : ""),
                    opt.isRequired() ? "Y" : "N",
                    opt.getDefaultValue() == null ? "" : opt.getDefaultValue());
        }
        t.sort(Table.SortType.ASCENDANT);
        return t.build();
    }

    public static void displayInfo(PmCommandInvocation invoc, Path installation,
            ProvisioningConfig config, String type, Function<ProvisioningLayout<FeaturePackLayout>, FeatureContainer> supplier) throws CommandExecutionException {
        try {
            if (!config.hasFeaturePackDeps()) {
                return;
            }
            invoc.println("");
            displayFeaturePacks(invoc, installation, config);

            if (type != null) {
                invoc.println("");
                try (ProvisioningLayout<FeaturePackLayout> layout = invoc.getPmSession().getLayoutFactory().newConfigLayout(config)) {
                    switch (type) {
                        case ALL: {
                            FeatureContainer container = supplier.apply(layout);
                            if (displayDependencies(invoc, layout)) {
                                invoc.println("");
                            }
                            if (displayPatches(invoc, layout)) {
                                invoc.println("");
                            }
                            if (displayConfigs(invoc, container)) {
                                invoc.println("");
                            }
                            displayOptions(invoc, layout);
                            break;
                        }
                        case CONFIGS: {
                            FeatureContainer container = supplier.apply(layout);
                            String configs = buildConfigs(invoc, container);
                            if (configs != null) {
                                invoc.print(configs);
                            } else {
                                invoc.println(NO_CONFIGURATIONS);
                            }
                            break;
                        }
                        case DEPENDENCIES: {
                            String deps = buildDependencies(invoc, layout);
                            if (deps != null) {
                                invoc.print(deps);
                            } else {
                                invoc.println(NO_DEPENDENCIES);
                            }
                            break;
                        }
                        case OPTIONS: {
                            String options = buildOptions(layout);
                            if (options != null) {
                                invoc.print(options);
                            } else {
                                invoc.println(NO_OPTIONS);
                            }
                            break;
                        }
                        case PATCHES: {
                            String patches = buildPatches(invoc, layout);
                            if (patches != null) {
                                invoc.print(patches);
                            } else {
                                invoc.println(NO_PATCHES);
                            }
                            break;
                        }
                        default: {
                            throw new CommandExecutionException(CliErrors.invalidInfoType());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.infoFailed(), ex);
        }
    }

    private static boolean displayConfigs(PmCommandInvocation invoc, FeatureContainer container) {
        String str = buildConfigs(invoc, container);
        if (str != null) {
            invoc.print(str);
        }
        return str != null;
    }

    private static String buildConfigs(PmCommandInvocation invoc, FeatureContainer container) {
        return buildConfigs(container.getFinalConfigs());
    }

    private static void displayFeaturePacks(PmCommandInvocation invoc, Path installation,
            ProvisioningConfig config) {
        printFeaturePacks(invoc, installation, config.getFeaturePackDeps());
    }

    private static boolean displayDependencies(PmCommandInvocation invoc, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        String str = buildDependencies(invoc, layout);
        if (str != null) {
            invoc.print(str);
        }
        return str != null;
    }

    private static String buildDependencies(PmCommandInvocation invoc, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        Map<FPID, FeaturePackConfig> configs = new HashMap<>();
        List<FeaturePackLocation> dependencies = new ArrayList<>();
        for (FeaturePackLayout fpLayout : layout.getOrderedFeaturePacks()) {
            boolean isProduct = true;
            for (FeaturePackLayout fpLayout2 : layout.getOrderedFeaturePacks()) {
                if (fpLayout2.getSpec().hasTransitiveDep(fpLayout.getFPID().getProducer())
                        || fpLayout2.getSpec().getFeaturePackDep(fpLayout.getFPID().getProducer()) != null) {
                    isProduct = false;
                    break;
                }
            }
            if (!isProduct) {
                FeaturePackLocation loc = invoc.getPmSession().getExposedLocation(null, fpLayout.getFPID().getLocation());
                dependencies.add(loc);
                FeaturePackConfig transitiveConfig = layout.getConfig().getTransitiveDep(fpLayout.getFPID().getProducer());
                configs.put(loc.getFPID(), transitiveConfig);
            }
        }
        return buildDependencies(dependencies, configs);
    }

    private static boolean displayPatches(PmCommandInvocation invoc, ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        String str = buildPatches(invoc, layout);
        if (str != null) {
            invoc.print(str);
        }
        return str != null;
    }

    private static String buildOptions(ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        return buildOptions(PluginResolver.resolvePlugins(layout));
    }

    private static void displayOptions(PmCommandInvocation commandInvocation,
            ProvisioningLayout<FeaturePackLayout> layout) throws ProvisioningException {
        String str = buildOptions(layout);
        if (str != null) {
            commandInvocation.print(str);
        }
    }

}
