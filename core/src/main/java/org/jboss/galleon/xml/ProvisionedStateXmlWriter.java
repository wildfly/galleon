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
                pkg.addChild(ProvisionedConfigXmlWriter.getInstance().toElement(config));
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
