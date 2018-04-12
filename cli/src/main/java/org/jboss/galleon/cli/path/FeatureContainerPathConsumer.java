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
package org.jboss.galleon.cli.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.path.PathParser.PathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureContainerPathConsumer implements PathConsumer {

    public enum State {
        NOT_STARTED,
        UNKNOWN,
        FINAL,
        CONFIGS,
        DEPENDENCIES,
        DEPENDENCIES_ORIGIN,
        FEATURES,
        FEATURES_ORIGIN,
        FEATURES_CONTENT,
        PACKAGES_CONTENT,
        PACKAGES_ORIGIN,
        //MODULES_CONTENT,
        //MODULES_ORIGIN,
        CONFIG_MODEL,
        CONFIG_NAME,
        CONFIG_CONTENT
    }
    public static final String FEATURES = "feature-specs";
    public static final String DEPENDENCIES = "dependencies";
    public static final String PACKAGES = "packages";
    //public static final String MODULES = "modules";
    public static final String CONFIGS = "configs";
    public static final String FINAL = "final";
    public static final String ROOT = "" + PathParser.PATH_SEPARATOR;

    public static final Group EDIT_ROOT_GRP = Group.fromString("", "" + PathParser.PATH_SEPARATOR);
    public static final Group EXPLORE_ROOT_GRP = Group.fromString("", "" + PathParser.PATH_SEPARATOR);

    //private static final Group MODULES_GRP = Group.fromString("", MODULES);
    private static final Group PACKAGES_GRP = Group.fromString("", PACKAGES);
    private static final Group FINAL_GRP = Group.fromString("", "" + FINAL);
    private static final Group CONFIGS_GRP = Group.fromString("", "" + CONFIGS);
    private static final Group FEATURES_GRP = Group.fromString("", "" + FEATURES);
    private static final Group DEPENDENCIES_GRP = Group.fromString("", "" + DEPENDENCIES);

    public static final String FINAL_CONFIGS_PATH = PathParser.PATH_SEPARATOR + CONFIGS + PathParser.PATH_SEPARATOR + FINAL + PathParser.PATH_SEPARATOR;
    public static final String PACKAGES_PATH = PathParser.PATH_SEPARATOR + PACKAGES + PathParser.PATH_SEPARATOR;
    public static final String FEATURES_PATH = PathParser.PATH_SEPARATOR + FEATURES + PathParser.PATH_SEPARATOR;

    private static final Map<String, Group> EDIT_GROUPS = new HashMap();
    private static final Map<String, Group> EXPLORE_GROUPS = new HashMap();

    static {
        EDIT_ROOT_GRP.addGroup(PACKAGES_GRP);
        EDIT_ROOT_GRP.addGroup(FEATURES_GRP);
        EDIT_ROOT_GRP.addGroup(CONFIGS_GRP);
        EDIT_ROOT_GRP.addGroup(DEPENDENCIES_GRP);

        EXPLORE_ROOT_GRP.addGroup(PACKAGES_GRP);
        EXPLORE_ROOT_GRP.addGroup(FEATURES_GRP);
        EXPLORE_ROOT_GRP.addGroup(CONFIGS_GRP);

        CONFIGS_GRP.addGroup(FINAL_GRP);

        EDIT_GROUPS.put(PACKAGES, PACKAGES_GRP);
        EDIT_GROUPS.put(DEPENDENCIES, DEPENDENCIES_GRP);
        EDIT_GROUPS.put(CONFIGS, CONFIGS_GRP);
        EDIT_GROUPS.put(FEATURES, FEATURES_GRP);

        EXPLORE_GROUPS.put(PACKAGES, PACKAGES_GRP);
        EXPLORE_GROUPS.put(CONFIGS, CONFIGS_GRP);
        EXPLORE_GROUPS.put(FEATURES, FEATURES_GRP);
    }

    private State state = State.NOT_STARTED;
    private FeatureContainer info;
    private ConfigInfo config;
    private Group current;
    private String configModel;
    private String configName;
    private final boolean completion;
    private String packagesGav;
    private String featuresGav;
    private String dependencyGav;
    private Group rootGrp;
    private final Map<String, Group> groups;
    // Accept that the last item is an error when completion is enabled.
    private boolean inError;
    private boolean edit;

    public FeatureContainerPathConsumer(FeatureContainer info, boolean completion) {
        this.info = info;
        this.completion = completion;
        edit = info.isEdit();
        rootGrp = edit ? EDIT_ROOT_GRP : EXPLORE_ROOT_GRP;
        groups = edit ? EDIT_GROUPS : EXPLORE_GROUPS;
    }

    //@Override
    public void enterConfigurationModel(String model) throws PathConsumerException {
        List<ConfigInfo> configs = info.getFinalConfigs().get(model);
        if (configs == null) {
            if (completion) {
                if (inError) {
                    throw new PathConsumerException("no config for model " + model);
                } else {
                    inError = true;
                }
            } else {
                throw new PathConsumerException("no config for model " + model);
            }
        }
        configModel = model;
    }

    //@Override
    public void enterConfigurationName(String name) throws PathConsumerException {
        List<ConfigInfo> configs = info.getFinalConfigs().get(configModel);
        for (ConfigInfo c : configs) {
            if (c.getName().equals(name)) {
                config = c;
                break;
            }
        }
        if (config == null) {
            if (completion) {
                if (inError) {
                    throw new PathConsumerException("no config for name " + name);
                } else {
                    inError = true;
                }
            } else {
                throw new PathConsumerException("no config for name " + name);
            }
        }
        configName = name;
        configModel = null;
    }

    @Override
    public void enterNode(PathParser.Node node) throws PathConsumerException {
        if (node == null) {
            // end of path, incomplete.
            return;
        }
        switch (state) {
            case UNKNOWN: {
                String n = node.getName();
                if (n.equals(CONFIGS)) {
                    state = State.FINAL;
                } else if (n.equals(FEATURES)) {
                    state = State.FEATURES_ORIGIN;
                } else if (n.equals(PACKAGES)) {
                    state = State.PACKAGES_ORIGIN;
                } else if (edit && n.equals(DEPENDENCIES)) {
                    state = State.DEPENDENCIES_ORIGIN;
                } else if (completion) {
                    if (inError) {
                        throw new PathConsumerException("Unknown " + n);
                    } else {
                        inError = true;
                    }
                } else {
                    throw new PathConsumerException("Unknown " + n);
                }
                break;
            }
            case FINAL: {
                String n = node.getName();
                if (n.equals(FINAL)) {
                    state = State.CONFIG_MODEL;
                } else if (completion) {
                    if (inError) {
                        throw new PathConsumerException("Unknown " + n);
                    } else {
                        inError = true;
                    }
                } else {
                    throw new PathConsumerException("Unknown " + n);
                }
                break;
            }
            case CONFIG_MODEL: {
                // A configuration model.
                String n = node.getName();
                enterConfigurationModel(n);
                state = State.CONFIG_NAME;
                break;
            }
            case CONFIG_NAME: {
                // A configuration name.
                String n = node.getName();
                enterConfigurationName(n);
                state = State.CONFIG_CONTENT;
                break;
            }
            case CONFIG_CONTENT: {
                enterContent(node);
                break;
            }
            case FEATURES_ORIGIN: {
                featuresGav = node.getName();
                state = State.FEATURES_CONTENT;
                break;
            }
            case FEATURES_CONTENT: {
                enterFeaturesContent(node);
                break;
            }
            case DEPENDENCIES_ORIGIN: {
                dependencyGav = node.getName();
                info = info.getFullDependencies().get(dependencyGav);
                if (info == null) {
                    throw new PathConsumerException("Unknown dependency " + dependencyGav);
                }
                // No dependencies inside a dependency.
                rootGrp = EXPLORE_ROOT_GRP;
                state = State.UNKNOWN;
                break;
            }
            case PACKAGES_ORIGIN: {
                packagesGav = node.getName();
                state = State.PACKAGES_CONTENT;
                break;
            }
            case PACKAGES_CONTENT: {
                enterPackagesContent(node);
                break;
            }
        }
    }

    private void enterPackagesContent(PathParser.Node node) throws PathConsumerException {
        Group next = null;
        if (current == null) {
            current = info.getPackages().get(packagesGav);
        }
        for (Group info : current.getGroups()) {
            if (info.getIdentity().getName().equals(node.getName())) {
                next = info;
                break;
            }
        }
        if (next == null) {
            if (completion) {
                if (inError) {
                    throw new PathConsumerException("no node for name " + node.getName());
                } else {
                    inError = true;
                }
            } else {
                throw new PathConsumerException("no node for name " + node.getName());
            }
        } else {
            current = next;
        }
        packagesGav = null;
    }

    private void enterFeaturesContent(PathParser.Node node) throws PathConsumerException {
        Group next = null;
        if (current == null) {
            current = info.getFeatureSpecs().get(featuresGav);
        }
        for (Group info : current.getGroups()) {
            if (info.getIdentity().getName().equals(node.getName())) {
                next = info;
                break;
            }
        }
        if (next == null) {
            if (completion) {
                if (inError) {
                    throw new PathConsumerException("no node for name " + node.getName());
                } else {
                    inError = true;
                }
            } else {
                throw new PathConsumerException("no node for name " + node.getName());
            }
        } else {
            current = next;
        }
        featuresGav = null;
    }

    private void enterContent(PathParser.Node node) throws PathConsumerException {
        Group next = null;
        if (current == null) {
            current = config.getRoot();
        }
        for (Group info : current.getGroups()) {
            if (info.getIdentity().getName().equals(node.getName())) {
                next = info;
                break;
            }
        }
        if (next == null) {
            if (completion) {
                if (inError) {
                    throw new PathConsumerException("no node for name " + node.getName());
                } else {
                    inError = true;
                }
            } else {
                throw new PathConsumerException("no node for name " + node.getName());
            }
        } else {
            current = next;
        }
    }

    @Override
    public void enterRoot() throws PathConsumerException {
        state = State.UNKNOWN;
    }

    public String getConfigModel() {
        return configModel;
    }

    public String getConfigName() {
        return configName;
    }

    public ConfigInfo getConfig() {
        return config;
    }

    public State getState() {
        return state;
    }

    public String getPackagesOrigin() {
        return packagesGav;
    }

    public List<String> getCandidates(String path) {
        List<String> candidates = new ArrayList<>();
        boolean completePath = path.endsWith("" + PathParser.PATH_SEPARATOR);
        String chunk = completePath ? null : path.substring(path.lastIndexOf("" + PathParser.PATH_SEPARATOR) + 1);
        switch (state) {
            case UNKNOWN: {
                if (chunk != null) {
                    if (CONFIGS.equals(chunk) || FEATURES.equals(chunk) || PACKAGES.equals(chunk) || edit && DEPENDENCIES.equals(chunk)) {
                        candidates.add(chunk + PathParser.PATH_SEPARATOR);
                    } else if (CONFIGS.startsWith(chunk)) {
                        candidates.add(CONFIGS);
                    } else if (FEATURES.startsWith(chunk)) {
                        candidates.add(FEATURES);
                    } else if (PACKAGES.startsWith(chunk)) {
                        candidates.add(PACKAGES);
                    } else if (edit && DEPENDENCIES.startsWith(chunk)) {
                        candidates.add(DEPENDENCIES);
                    }
                } else {
                    for (Group g : rootGrp.getGroups()) {
                        candidates.add(g.getIdentity().getName() + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
            case FINAL: {
                if (chunk != null) {
                    if (FINAL.equals(chunk)) {
                        candidates.add(chunk + PathParser.PATH_SEPARATOR);
                    } else if (FINAL.startsWith(chunk)) {
                        candidates.add(FINAL);
                    }
                } else {
                    candidates.add(FINAL + PathParser.PATH_SEPARATOR);
                }
                break;
            }
            case CONFIG_MODEL: {
                if (chunk != null) {
                    for (String model : info.getFinalConfigs().keySet()) {
                        if (model.equals(chunk)) {
                            candidates.add(chunk + PathParser.PATH_SEPARATOR);
                        } else if (model.startsWith(chunk)) {
                            candidates.add(model);
                        }
                    }
                } else {
                    for (String model : info.getFinalConfigs().keySet()) {
                        candidates.add(model + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
            case CONFIG_NAME: {
                if (chunk != null) {
                    for (ConfigInfo cf : info.getFinalConfigs().get(configModel)) {
                        if (cf.getName().equals(chunk)) {
                            candidates.add(chunk + PathParser.PATH_SEPARATOR);
                        } else if (cf.getName().startsWith(chunk)) {
                            candidates.add(cf.getName());
                        }
                    }
                } else {
                    for (ConfigInfo cf : info.getFinalConfigs().get(configModel)) {
                        candidates.add(cf.getName() + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
            case FEATURES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getFeatureSpecs().keySet()) {
                        if (cf.equals(chunk)) {
                            candidates.add(chunk + PathParser.PATH_SEPARATOR);
                        } else if (cf.startsWith(chunk)) {
                            candidates.add(cf);
                        }
                    }
                } else {
                    for (String cf : info.getFeatureSpecs().keySet()) {
                        candidates.add(cf + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
            case DEPENDENCIES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getFullDependencies().keySet()) {
                        if (cf.equals(chunk)) {
                            candidates.add(chunk + PathParser.PATH_SEPARATOR);
                        } else if (cf.startsWith(chunk)) {
                            candidates.add(cf);
                        }
                    }
                } else {
                    for (String cf : info.getFullDependencies().keySet()) {
                        candidates.add(cf + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
            case PACKAGES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getPackages().keySet()) {
                        if (cf.equals(chunk)) {
                            candidates.add(chunk + PathParser.PATH_SEPARATOR);
                        } else if (cf.startsWith(chunk)) {
                            candidates.add(cf);
                        }
                    }
                } else {
                    for (String cf : info.getPackages().keySet()) {
                        candidates.add(cf + PathParser.PATH_SEPARATOR);
                    }
                }
                break;
            }
//            case MODULES_CONTENT: {
//                if (current == null) {
//                    current = info.getModules().get(modulesGav);
//                }
//            }
            case PACKAGES_CONTENT: {
                if (current == null) {
                    current = info.getPackages().get(packagesGav);
                }
            }
            case FEATURES_CONTENT: {
                if (current == null) {
                    current = info.getFeatureSpecs().get(featuresGav);
                }
            }
            case CONFIG_CONTENT: {
                if (current == null) {
                    current = config.getRoot();
                }
                if (chunk != null) {
                    for (Group fg : current.getGroups()) {
                        if (fg.getIdentity().getName().equals(chunk)) {
                            if (!fg.getGroups().isEmpty()) {
                                candidates.add(chunk + PathParser.PATH_SEPARATOR);
                            } else {
                                candidates.add(fg.getIdentity().getName());
                            }
                        } else if (fg.getIdentity().getName().startsWith(chunk)) {
                            candidates.add(fg.getIdentity().getName());
                        }
                    }
                } else {
                    for (Group fg : current.getGroups()) {
                        candidates.add(fg.getIdentity().getName() + (fg.getGroups().isEmpty() ? "" : PathParser.PATH_SEPARATOR));
                    }
                }
                break;
            }
        }
        return candidates;
    }

    @Override
    public boolean expectEndOfNode() {
        return true;
    }

    public Group getCurrentNode(String path) throws PathConsumerException {
        boolean completePath = path.endsWith("" + PathParser.PATH_SEPARATOR);
        String chunk = completePath ? null : path.substring(path.lastIndexOf("" + PathParser.PATH_SEPARATOR) + 1);
        switch (state) {
            case UNKNOWN: {
                if (chunk != null) {
                    Group grp = groups.get(chunk);
                    if (grp == null) {
                        throw new PathConsumerException("Unknown " + chunk);
                    }
                    return grp;
                } else {
                    return rootGrp;
                }
            }
            case FINAL: {
                if (chunk != null) {
                    if (FINAL.equals(chunk)) {
                        Group grp = Group.fromString("", FINAL);
                        for (String model : info.getFinalConfigs().keySet()) {
                            grp.addGroup(Group.fromString("", model));
                        }
                        return grp;
                    } else {
                        throw new PathConsumerException("Unknown " + chunk);
                    }
                } else {
                    return CONFIGS_GRP;
                }
            }
            case CONFIG_MODEL: {
                if (chunk != null) {
                    String m = null;
                    for (String model : info.getFinalConfigs().keySet()) {
                        if (model.equals(chunk)) {
                            m = model;
                            break;
                        }
                    }
                    if (m == null) {
                        throw new PathConsumerException("Unknown " + chunk);
                    }
                    Group grp = Group.fromString("", m);
                    for (ConfigInfo cf : info.getFinalConfigs().get(m)) {
                        grp.addGroup(Group.fromString("", cf.getName()));
                    }
                    return grp;
                } else {
                    Group grp = Group.fromString("", FINAL);
                    for (String model : info.getFinalConfigs().keySet()) {
                        grp.getGroups().add(Group.fromString("", model));
                    }
                    return grp;
                }
            }
            case CONFIG_NAME: {
                if (chunk != null) {
                    ConfigInfo c = null;
                    for (ConfigInfo cf : info.getFinalConfigs().get(configModel)) {
                        if (cf.getName().equals(chunk)) {
                            Group grp = Group.fromString("", cf.getName());
                            grp.getGroups().addAll(cf.getRoot().getGroups());
                            return grp;
                        }
                    }
                    throw new PathConsumerException("Unknown " + chunk);
                } else {
                    Group grp = Group.fromString("", configModel);
                    for (ConfigInfo cf : info.getFinalConfigs().get(configModel)) {
                        grp.getGroups().add(Group.fromString("", cf.getName()));
                    }
                    return grp;
                }
            }
            case FEATURES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getFeatureSpecs().keySet()) {
                        if (cf.equals(chunk)) {
                            Group grp = Group.fromString("", cf);
                            grp.getGroups().addAll(info.getFeatureSpecs().get(cf).getGroups());
                            return grp;
                        }
                    }
                    throw new PathConsumerException("Unknown " + chunk);
                } else {
                    Group grp = Group.fromString("", FEATURES);
                    for (String cf : info.getFeatureSpecs().keySet()) {
                        grp.getGroups().add(Group.fromString("", cf));
                    }
                    return grp;
                }
            }
            case DEPENDENCIES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getFullDependencies().keySet()) {
                        if (cf.equals(chunk)) {
                            return EXPLORE_ROOT_GRP;
                        }
                    }
                    throw new PathConsumerException("Unknown " + chunk);
                } else {
                    Group grp = Group.fromString("", DEPENDENCIES);
                    for (String cf : info.getFullDependencies().keySet()) {
                        grp.getGroups().add(Group.fromString("", cf));
                    }
                    return grp;
                }
            }
            case PACKAGES_ORIGIN: {
                if (chunk != null) {
                    for (String cf : info.getPackages().keySet()) {
                        if (cf.equals(chunk)) {
                            Group grp = Group.fromString("", cf);
                            grp.getGroups().addAll(info.getPackages().get(cf).getGroups());
                            return grp;
                        }
                    }
                    throw new PathConsumerException("Unknown " + chunk);
                } else {
                    Group grp = Group.fromString("", PACKAGES);
                    for (String cf : info.getPackages().keySet()) {
                        grp.getGroups().add(Group.fromString("", cf));
                    }
                    return grp;
                }
            }
//            case MODULES_CONTENT: {
//                if (current == null) {
//                    Group grp = Group.fromString("", modulesGav);
//                    grp.getGroups().addAll(info.getModules().get(modulesGav).getGroups());
//                    current = grp;
//                }
//            }
            case PACKAGES_CONTENT: {
                if (current == null) {
                    Group grp = Group.fromString("", packagesGav);
                    grp.getGroups().addAll(info.getPackages().get(packagesGav).getGroups());
                    current = grp;
                }
            }
            case FEATURES_CONTENT: {
                if (current == null) {
                    Group grp = Group.fromString("", featuresGav);
                    grp.getGroups().addAll(info.getFeatureSpecs().get(featuresGav).getGroups());
                    current = grp;
                }
            }
            case CONFIG_CONTENT: {
                if (current == null) {
                    Group grp = Group.fromString("", config.getName());
                    grp.getGroups().addAll(config.getRoot().getGroups());
                    current = grp;
                }
                if (chunk != null) {
                    for (Group fg : current.getGroups()) {
                        if (fg.getIdentity().getName().equals(chunk)) {
                            return fg;
                        }
                    }
                    throw new PathConsumerException("Unknown " + chunk);
                } else {
                    return current;
                }
            }
        }
        return null;
    }

}
