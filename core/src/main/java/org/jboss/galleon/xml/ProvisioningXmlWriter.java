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
package org.jboss.galleon.xml;

import java.util.Arrays;
import java.util.Map;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.config.ConfigCustomizations;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.PackageConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlParser10.Attribute;
import org.jboss.galleon.xml.ProvisioningXmlParser10.Element;
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

    protected ElementNode toElement(ProvisioningConfig provisioningConfig) {

        final ElementNode install = addElement(null, Element.INSTALLATION);

        if (provisioningConfig.hasFeaturePackDeps()) {
            for(FeaturePackConfig fp : provisioningConfig.getFeaturePackDeps()) {
                final ElementNode fpElement = addElement(install, Element.FEATURE_PACK);
                writeFeaturePackConfig(fpElement, fpElement.getNamespace(), fp, provisioningConfig.originOf(fp.getGav().toGa()));
            }
        }

        writeConfigCustomizations(install,Element.INSTALLATION.getNamespace(), provisioningConfig);

        return install;
    }

    static void writeFeaturePackConfig(ElementNode fp, String ns, FeaturePackConfig featurePack, String origin) {
        addGav(fp, featurePack.getGav());
        if(origin != null) {
            addElement(fp, Element.ORIGIN.getLocalName(), ns).addChild(new TextNode(origin));
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
            for (PackageConfig included : featurePack.getIncludedPackages()) {
                final ElementNode include = addElement(packages, Element.INCLUDE.getLocalName(), ns);
                addAttribute(include, Attribute.NAME, included.getName());
            }
        }
    }

    static void writeConfigCustomizations(ElementNode parent, String ns, ConfigCustomizations configCustoms) {

        ElementNode defConfigsE = null;

        if(!configCustoms.isInheritConfigs()) {
            defConfigsE = addElement(parent, Element.DEFAULT_CONFIGS.getLocalName(), ns);
            addAttribute(defConfigsE, Attribute.INHERIT, FALSE);
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

    static void addGav(final ElementNode fp, final ArtifactCoords.Gav fpGav) {
        addAttribute(fp, Attribute.GROUP_ID, fpGav.getGroupId());
        addAttribute(fp, Attribute.ARTIFACT_ID, fpGav.getArtifactId());
        if (fpGav.getVersion() != null) {
            addAttribute(fp, Attribute.VERSION, fpGav.getVersion());
        }
    }
}
