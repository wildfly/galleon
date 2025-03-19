/*
 * Copyright 2016-2025 Red Hat, Inc. and/or its affiliates
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
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureSpec;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.xml.ProvisionedConfigXmlParser30.Attribute;
import org.jboss.galleon.xml.ProvisionedConfigXmlParser30.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedConfigXmlWriter extends BaseXmlWriter<ProvisionedConfig> {

    private static class XmlConfigHandler implements ProvisionedConfigHandler {

        private final ElementNode parent;
        private ElementNode fpElement;
        private ElementNode specElement;

        XmlConfigHandler(ElementNode parent) {
            this.parent = parent;
        }

        @Override
        public void nextFeaturePack(FPID fpid) {
            fpElement = addElement(parent, Element.FEATURE_PACK.getLocalName(), parent.getNamespace());
            addAttribute(fpElement, Attribute.LOCATION, fpid.toString());
        }

        @Override
        public void nextSpec(ResolvedFeatureSpec spec) {
            specElement = addElement(fpElement, Element.SPEC.getLocalName(), parent.getNamespace());
            addAttribute(specElement, Attribute.NAME, spec.getId().getName());
        }

        @Override
        public void nextFeature(ProvisionedFeature feature) throws ProvisioningException {
            final ElementNode featureE = addElement(specElement, Element.FEATURE.getLocalName(), parent.getNamespace());
            if(feature.hasId()) {
                addAttribute(featureE, Attribute.ID, feature.getId().toString());
            }
            if(feature.hasParams()) {
                for(String param : new TreeSet<>(feature.getParamNames())) {
                    final ElementNode paramE = addElement(featureE, Element.PARAM.getLocalName(), parent.getNamespace());
                    addAttribute(paramE, Attribute.NAME, param);
                    addAttribute(paramE, Attribute.VALUE, feature.getConfigParam(param));
                }
            }
        }
    }

    private static final ProvisionedConfigXmlWriter INSTANCE = new ProvisionedConfigXmlWriter();

    public static ProvisionedConfigXmlWriter getInstance() {
        return INSTANCE;
    }

    private ProvisionedConfigXmlWriter() {
    }

    protected ElementNode toElement(ProvisionedConfig config) throws XMLStreamException {
        return toElement(config, ProvisionedConfigXmlParser30.NAMESPACE_3_0);
    }

    protected ElementNode toElement(ProvisionedConfig config, String ns) throws XMLStreamException {

        final ElementNode configE = addElement(null, Element.CONFIG.getLocalName(), ns);
        if(config.getName() != null) {
            addAttribute(configE, Attribute.NAME, config.getName());
        }
        if(config.getModel() != null) {
            addAttribute(configE, Attribute.MODEL, config.getModel());
        }

        if(config.hasProperties()) {
            final ElementNode propsE = addElement(configE, Element.PROPS.getLocalName(), ns);
            for(Map.Entry<String, String> entry : new TreeMap<>(config.getProperties()).entrySet()) {
                final ElementNode propE = addElement(propsE, Element.PROP.getLocalName(), ns);
                addAttribute(propE, Attribute.NAME, entry.getKey());
                addAttribute(propE, Attribute.VALUE, entry.getValue());
            }
        }

        if(config.hasLayers()) {
            final ElementNode propsE = addElement(configE, Element.LAYERS.getLocalName(), ns);
            for(ConfigId layerId : config.getLayers()) {
                final ElementNode propE = addElement(propsE, Element.LAYER.getLocalName(), ns);
                if(layerId.getModel() != null) {
                    addAttribute(propE, Attribute.MODEL, layerId.getModel());
                }
                addAttribute(propE, Attribute.NAME, layerId.getName());
            }
        }

        if(config.hasFeatures()) {
            try {
                config.handle(new XmlConfigHandler(configE));
            } catch (ProvisioningException e) {
                throw new XMLStreamException("Failed to marshal ProvisionedConfig", e);
            }
        }
        return configE;
    }
}
