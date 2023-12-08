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
package org.jboss.galleon.xml;

import java.util.Map;
import org.jboss.galleon.config.ConfigId;

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.xml.ConfigXml.Attribute;
import org.jboss.galleon.xml.ConfigXml.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigXmlWriter extends BaseXmlWriter<ConfigModel> {

    private static final ConfigXmlWriter INSTANCE = new ConfigXmlWriter();

    public static ConfigXmlWriter getInstance() {
        return INSTANCE;
    }

    private ConfigXmlWriter() {
    }

    protected ElementNode toElement(ConfigModel config) {
        return toElement(config, ConfigXml.NAMESPACE_1_0);
    }

    protected ElementNode toElement(ConfigModel config, String ns) {
        final ElementNode configE = addElement(null, Element.CONFIG.getLocalName(), ns);
        if(config.getModel() != null) {
            addAttribute(configE, Attribute.MODEL, config.getModel());
        }

        if(config.hasProperties()) {
            final ElementNode propsE = addElement(configE, Element.PROPS.getLocalName(), ns);
            for(Map.Entry<String, String> prop : config.getProperties().entrySet()) {
                final ElementNode propE = addElement(propsE, Element.PROP.getLocalName(), ns);
                addAttribute(propE, Attribute.NAME, prop.getKey());
                addAttribute(propE, Attribute.VALUE, prop.getValue());
            }
        }

        if(config.hasConfigDeps()) {
            final ElementNode configDeps = addElement(configE, Element.CONFIG_DEPS.getLocalName(), ns);
            for(Map.Entry<String, ConfigId> dep : config.getConfigDeps().entrySet()) {
                final ElementNode configDep = addElement(configDeps, Element.CONFIG_DEP.getLocalName(), ns);
                addAttribute(configDep, Attribute.ID.getLocalName(), dep.getKey());
                final ConfigId configId = dep.getValue();
                if(configId.getModel() != null) {
                    addAttribute(configDep, Attribute.MODEL.getLocalName(), configId.getModel());
                }
                if(configId.getName() != null) {
                    addAttribute(configDep, Attribute.NAME.getLocalName(), configId.getName());
                }
            }
        }

        ElementNode layers = null;
        if(!config.isInheritLayers()) {
            layers = addElement(configE, Element.LAYERS.getLocalName(), ns);
            addAttribute(layers, Attribute.INHERIT, "false");
        }
        if(config.hasIncludedLayers()) {
            if(layers == null) {
                layers = addElement(configE, Element.LAYERS.getLocalName(), ns);
            }
            for(String layerDep : config.getIncludedLayers()) {
                final ElementNode layer = addElement(layers, Element.INCLUDE.getLocalName(), ns);
                addAttribute(layer, Attribute.NAME, layerDep);
            }
        }
        if(config.hasExcludedLayers()) {
            if(layers == null) {
                layers = addElement(configE, Element.LAYERS.getLocalName(), ns);
            }
            for(String layerDep : config.getExcludedLayers()) {
                final ElementNode layer = addElement(layers, Element.EXCLUDE.getLocalName(), ns);
                addAttribute(layer, Attribute.NAME, layerDep);
            }
        }

        FeatureGroupXmlWriter.addFeatureGroupDepBody(config, configE, ns);
        return configE;
    }
}
