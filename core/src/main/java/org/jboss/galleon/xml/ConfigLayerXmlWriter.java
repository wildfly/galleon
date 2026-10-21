/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import org.jboss.galleon.api.GalleonLayerDependency;
import org.jboss.galleon.spec.ConfigLayerSpec;
import org.jboss.galleon.xml.ConfigLayerXml.Attribute;
import org.jboss.galleon.xml.ConfigLayerXml.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayerXmlWriter extends BaseXmlWriter<ConfigLayerSpec> {

    private static final ConfigLayerXmlWriter INSTANCE = new ConfigLayerXmlWriter();

    public static ConfigLayerXmlWriter getInstance() {
        return INSTANCE;
    }

    private ConfigLayerXmlWriter() {
    }

    protected ElementNode toElement(ConfigLayerSpec layer) {
        return toElement(layer, ConfigLayerXml.NAMESPACE_1_0);
    }

    protected ElementNode toElement(ConfigLayerSpec layer, String ns) {
        final ElementNode configE = addElement(null, Element.LAYER_SPEC.getLocalName(), ns);
        addAttribute(configE, Attribute.NAME, layer.getName());

        if(layer.hasLayerDeps()) {
            final ElementNode layers = addElement(configE, Element.DEPENDENCIES.getLocalName(), ns);
            for(GalleonLayerDependency layerDep : layer.getLayerDeps()) {
                final ElementNode layerE = addElement(layers, Element.LAYER.getLocalName(), ns);
                addAttribute(layerE, Attribute.NAME, layerDep.getName());
                if(layerDep.isOptional()) {
                    addAttribute(layerE, Attribute.OPTIONAL, "true");
                }
            }
        }

        FeatureGroupXmlWriter.addFeatureGroupDepBody(layer, configE, ns);
        return configE;
    }
}
