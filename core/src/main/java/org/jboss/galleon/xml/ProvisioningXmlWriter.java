/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.xml;

import java.util.Arrays;
import java.util.Map;

import org.jboss.galleon.config.ConfigCustomizations;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.FeaturePackDepsConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.xml.ProvisioningXmlParser30.Attribute;
import org.jboss.galleon.xml.ProvisioningXmlParser30.Element;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.TextNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningXmlWriter extends BaseXmlWriter<ProvisioningConfig> {

    private static final ProvisioningXmlWriter INSTANCE = new ProvisioningXmlWriter();

    private static final String FALSE = "false";

    public static ProvisioningXmlWriter getInstance() {
        return INSTANCE;
    }

    private ProvisioningXmlWriter() {
    }

    protected ElementNode toElement(ProvisioningConfig config) {

        final ElementNode install = addElement(null, Element.INSTALLATION);

        writeUniverseSpecs(config, install);

        if(config.hasTransitiveDeps()) {
            final ElementNode transitives = addElement(install, Element.TRANSITIVE);
            for(FeaturePackConfig dep : config.getTransitiveDeps()) {
                writeFeaturePackConfig(addElement(transitives, Element.FEATURE_PACK),
                        config.getUserConfiguredLocation(dep.getLocation()), dep,
                        config.originOf(dep.getLocation().getProducer()));
            }
        }

        if (config.hasFeaturePackDeps()) {
            for(FeaturePackConfig fp : config.getFeaturePackDeps()) {
                final ElementNode fpElement = addElement(install, Element.FEATURE_PACK);
                writeFeaturePackConfig(fpElement, config.getUserConfiguredLocation(fp.getLocation()),
                        fp, config.originOf(fp.getLocation().getProducer()));
            }
        }

        writeConfigCustomizations(install, Element.INSTALLATION.getNamespace(), config);

        if(config.hasOptions()) {
            final Map<String, String> pluginOptions = config.getOptions();
            final String[] names = pluginOptions.keySet().toArray(new String[pluginOptions.size()]);
            Arrays.sort(names);
            final ElementNode optionsE = addElement(install, Element.OPTIONS);
            for(String name : names) {
                final ElementNode optionE = addElement(optionsE, Element.OPTION);
                addAttribute(optionE, Attribute.NAME, name);
                final String value = pluginOptions.get(name);
                if(value != null) {
                    addAttribute(optionE, Attribute.VALUE, value);
                }
            }
        }

        return install;
    }

    static void writeUniverseSpecs(FeaturePackDepsConfig fpDeps, final ElementNode parent) {
        ElementNode universesEl = null;
        UniverseSpec universeSpec = fpDeps.getDefaultUniverse();
        if(universeSpec != null) {
            universesEl = addElement(parent, Element.UNIVERSES.getLocalName(), parent.getNamespace());
            writeUniverseConfig(universesEl, null, universeSpec.getFactory(), universeSpec.getLocation());
        }
        if(fpDeps.hasUniverseNamedSpecs()) {
            if(universesEl == null) {
                universesEl = addElement(parent, Element.UNIVERSES.getLocalName(), parent.getNamespace());
            }
            for(Map.Entry<String, UniverseSpec> universe : fpDeps.getUniverseNamedSpecs().entrySet()) {
                writeUniverseConfig(universesEl, universe.getKey(), universe.getValue().getFactory(), universe.getValue().getLocation());
            }
        }
    }

    private static void writeUniverseConfig(ElementNode universesEl, String name, String factory, String location) {
        final ElementNode universeEl = addElement(universesEl, Element.UNIVERSE.getLocalName(), universesEl.getNamespace());
        if(name != null) {
            addAttribute(universeEl, Attribute.NAME, name);
        }
        addAttribute(universeEl, Attribute.FACTORY, factory);
        if(location != null) {
            addAttribute(universeEl, Attribute.LOCATION, location);
        }
    }

    static void writeFeaturePackConfig(ElementNode fp, FeaturePackLocation location, FeaturePackConfig featurePack, String origin) {

        final String ns = fp.getNamespace();
        addAttribute(fp, Attribute.LOCATION, location.toString());
        if(origin != null) {
            addElement(fp, Element.ORIGIN.getLocalName(), ns).addChild(new TextNode(origin));
        }

        if(featurePack.hasPatches()) {
            final ElementNode patches = addElement(fp, Element.PATCHES.getLocalName(), ns);
            for(FPID patchId : featurePack.getPatches()) {
                final ElementNode patch = addElement(patches, Element.PATCH.getLocalName(), ns);
                addAttribute(patch, Attribute.ID, patchId.toString());
            }
        }

        writeConfigCustomizations(fp, ns, featurePack);

        ElementNode packages = null;
        if (!featurePack.isInheritPackages()) {
            packages = addElement(fp, Element.PACKAGES.getLocalName(), ns);
            addAttribute(packages, Attribute.INHERIT, FALSE);
        }
        if (featurePack.hasExcludedPackages()) {
            if (packages == null) {
                packages = addElement(fp, Element.PACKAGES.getLocalName(), ns);
            }
            for (String excluded : featurePack.getExcludedPackages()) {
                final ElementNode exclude = addElement(packages, Element.EXCLUDE.getLocalName(), ns);
                addAttribute(exclude, Attribute.NAME, excluded);
            }
        }
        if (featurePack.hasIncludedPackages()) {
            if (packages == null) {
                packages = addElement(fp, Element.PACKAGES.getLocalName(), ns);
            }
            for (String included : featurePack.getIncludedPackages()) {
                final ElementNode include = addElement(packages, Element.INCLUDE.getLocalName(), ns);
                addAttribute(include, Attribute.NAME, included);
            }
        }
    }

    static void writeConfigCustomizations(ElementNode parent, String ns, ConfigCustomizations configCustoms) {

        ElementNode defConfigsE = null;

        final Boolean inheritConfigs = configCustoms.getInheritConfigs();
        if(inheritConfigs != null) {
            defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            addAttribute(defConfigsE, Attribute.INHERIT, inheritConfigs.toString());
        }
        if(!configCustoms.isInheritModelOnlyConfigs()) {
            if(defConfigsE == null) {
                defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            }
            addAttribute(defConfigsE, Attribute.INHERIT_UNNAMED_MODELS, FALSE);
        }
        if(configCustoms.hasFullModelsExcluded()) {
            if(defConfigsE == null) {
                defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            }
            for (Map.Entry<String, Boolean> excluded : configCustoms.getFullModelsExcluded().entrySet()) {
                final ElementNode exclude = addElement(defConfigsE, Element.EXCLUDE.getLocalName(), ns);
                addAttribute(exclude, Attribute.MODEL, excluded.getKey());
                if(!excluded.getValue()) {
                    addAttribute(exclude, Attribute.NAMED_MODELS_ONLY, FALSE);
                }
            }
        }
        if(configCustoms.hasFullModelsIncluded()) {
            if(defConfigsE == null) {
                defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            }
            final String[] array = configCustoms.getFullModelsIncluded().toArray(new String[configCustoms.getFullModelsIncluded().size()]);
            Arrays.sort(array);
            for(String modelName : array) {
                final ElementNode included = addElement(defConfigsE, Element.INCLUDE.getLocalName(), ns);
                addAttribute(included, Attribute.MODEL, modelName);
            }
        }
        if(configCustoms.hasExcludedConfigs()) {
            if(defConfigsE == null) {
                defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            }
            for(ConfigId configId : configCustoms.getExcludedConfigs()) {
                final ElementNode excluded = addElement(defConfigsE, Element.EXCLUDE.getLocalName(), ns);
                if(configId.getModel() != null) {
                    addAttribute(excluded, Attribute.MODEL, configId.getModel());
                }
                if(configId.getName() != null) {
                    addAttribute(excluded, Attribute.NAME, configId.getName());
                }
            }
        }
        if(configCustoms.hasIncludedConfigs()) {
            if(defConfigsE == null) {
                defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            }
            for (ConfigId config : configCustoms.getIncludedConfigs()) {
                final ElementNode includeElement = addElement(defConfigsE, Element.INCLUDE.getLocalName(), ns);
                if(config.getModel() != null) {
                    addAttribute(includeElement, Attribute.MODEL, config.getModel());
                }
                if(config.getName() != null) {
                    addAttribute(includeElement, Attribute.NAME, config.getName());
                }
            }
        }

        if(configCustoms.hasDefinedConfigs()) {
            for (ConfigModel config : configCustoms.getDefinedConfigs()) {
                parent.addChild(ConfigXmlWriter.getInstance().toElement(config, ns));
            }
        }
    }
}
