/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.state.FeaturePack;
import org.jboss.galleon.state.FeaturePackPackage;
import org.jboss.galleon.state.FeaturePackSet;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.xml.ProvisionedStateXmlParser30.Attribute;
import org.jboss.galleon.xml.ProvisionedStateXmlParser30.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedStateXmlWriter extends BaseXmlWriter<FeaturePackSet<?>> {

    private static final ProvisionedStateXmlWriter INSTANCE = new ProvisionedStateXmlWriter();

    public static ProvisionedStateXmlWriter getInstance() {
        return INSTANCE;
    }

    private ProvisionedStateXmlWriter() {
    }

    @Override
    protected ElementNode toElement(FeaturePackSet<?> provisionedState) throws XMLStreamException {

        final ElementNode pkg = addElement(null, Element.INSTALLATION);

        if (provisionedState.hasFeaturePacks()) {
            for(FeaturePack<?> fp : provisionedState.getFeaturePacks()) {
                final ElementNode fpElement = addElement(pkg, Element.FEATURE_PACK);
                writeFeaturePack(fpElement, fp);
            }
        }

        if(provisionedState.hasConfigs()) {
            for(ProvisionedConfig config : provisionedState.getConfigs()) {
                pkg.addChild(ProvisionedConfigXmlWriter.getInstance().toElement(config, Element.CONFIG.getNamespace()));
            }
        }

        return pkg;
    }

    private void writeFeaturePack(ElementNode fp, FeaturePack<?> featurePack) {
        addAttribute(fp, Attribute.LOCATION, featurePack.getFPID().toString());

        if (featurePack.hasPackages()) {
            final ElementNode packages = addElement(fp, Element.PACKAGES);
            for (FeaturePackPackage pkg : featurePack.getPackages()) {
                final ElementNode pkgElement = addElement(packages, Element.PACKAGE);
                addAttribute(pkgElement, Attribute.NAME, pkg.getName());
            }
        }
    }
}
