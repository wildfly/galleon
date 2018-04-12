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

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.xml.FeaturePackXmlParser10.Attribute;
import org.jboss.galleon.xml.FeaturePackXmlParser10.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackXmlWriter extends BaseXmlWriter<FeaturePackSpec> {

    private static final FeaturePackXmlWriter INSTANCE = new FeaturePackXmlWriter();

    public static FeaturePackXmlWriter getInstance() {
        return INSTANCE;
    }

    private FeaturePackXmlWriter() {
    }

    protected ElementNode toElement(FeaturePackSpec fpSpec) {
        final ElementNode fp = addElement(null, Element.FEATURE_PACK);
        final ArtifactCoords.Gav fpGav = fpSpec.getGav();
        ProvisioningXmlWriter.addGav(fp, fpGav);

        if (fpSpec.hasFeaturePackDeps()) {
            final ElementNode deps = addElement(fp, Element.DEPENDENCIES);
            for (FeaturePackConfig dep : fpSpec.getFeaturePackDeps()) {
                final ElementNode depElement = addElement(deps, Element.DEPENDENCY);
                ProvisioningXmlWriter.writeFeaturePackConfig(depElement, depElement.getNamespace(), dep, fpSpec.originOf(dep.getGav().toGa()));
            }
        }

        ProvisioningXmlWriter.writeConfigCustomizations(fp, Element.FEATURE_PACK.getNamespace(), fpSpec);

        if (fpSpec.hasDefaultPackages()) {
            final ElementNode pkgs = addElement(fp, Element.DEFAULT_PACKAGES);
            final String[] pkgNames = fpSpec.getDefaultPackageNames().toArray(new String[0]);
            Arrays.sort(pkgNames);
            for (String name : pkgNames) {
                addAttribute(addElement(pkgs, Element.PACKAGE), Attribute.NAME, name);
            }
        }

        return fp;
    }
}
