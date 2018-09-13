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

import java.util.Map;

import org.jboss.galleon.config.ConfigItem;
import org.jboss.galleon.config.ConfigItemContainer;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeatureGroupSupport;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.SpecId;
import org.jboss.galleon.xml.FeatureGroupXml.Attribute;
import org.jboss.galleon.xml.FeatureGroupXml.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureGroupXmlWriter extends BaseXmlWriter<FeatureGroup> {

    private static final String FALSE = "false";
    private static final String TRUE = "true";

    private static final FeatureGroupXmlWriter INSTANCE = new FeatureGroupXmlWriter();

    public static FeatureGroupXmlWriter getInstance() {
        return INSTANCE;
    }

    private FeatureGroupXmlWriter() {
    }

    protected ElementNode toElement(FeatureGroup config) {
        return toElement(config, FeatureGroupXml.NAMESPACE_1_0);
    }

    protected ElementNode toElement(FeatureGroup featureGroup, String ns) {
        final ElementNode fgE = addElement(null, Element.FEATURE_GROUP_SPEC.getLocalName(), ns);
        if(featureGroup.getName() != null) {
            addAttribute(fgE, Attribute.NAME, featureGroup.getName());
        }

        //addFeatureGroupIncludeExclude(featureGroup, ns, fgE);
        writeFeatureGroupSpecBody(fgE, featureGroup, ns);

        if(featureGroup.hasPackageDeps()) {
            PackageXmlWriter.writePackageDeps(featureGroup, addElement(fgE, Element.PACKAGES.getLocalName(), ns));
        }
        return fgE;
    }

    static void writeFeatureGroupSpecBody(final ElementNode configE, ConfigItemContainer featureGroup, String ns) {
        if(!featureGroup.hasItems()) {
            return;
        }
        String currentOrigin = null;
        ElementNode parent = configE;
        for(ConfigItem item : featureGroup.getItems()) {
            final String itemOrigin = item.getOrigin();
            if(itemOrigin != null) {
                if (!itemOrigin.equals(currentOrigin)) {
                    parent = addElement(configE, Element.ORIGIN.getLocalName(), ns);
                    addAttribute(parent, Attribute.NAME, itemOrigin);
                    currentOrigin = itemOrigin;
                }
            } else if(currentOrigin != null) {
                currentOrigin = null;
                parent = configE;
            }
            if(item.isGroup()) {
                writeFeatureGroupDependency(parent, (FeatureGroup) item, ns);
            } else {
                addFeatureConfig(parent, (FeatureConfig) item, ns);
            }
        }
    }

    private static void writeFeatureGroupDependency(ElementNode depsE, FeatureGroup dep, String ns) {
        final ElementNode depE = addElement(depsE, Element.FEATURE_GROUP.getLocalName(), ns);
        addFeatureGroupDepBody(dep, depE, ns);
    }

    public static void addFeatureGroupDepBody(FeatureGroupSupport dep, final ElementNode depE, String ns) {
        if(dep.getName() != null) {
            addAttribute(depE, Attribute.NAME, dep.getName());
        }
        if(!dep.isInheritFeatures()) {
            addAttribute(depE, Attribute.INHERIT_FEATURES, FALSE);
        }
        addFeatureGroupIncludeExclude(dep, ns, depE);
        writeFeatureGroupSpecBody(depE, dep, ns);
        if(dep.hasExternalFeatureGroups()) {
            for(Map.Entry<String, FeatureGroup> entry : dep.getExternalFeatureGroups().entrySet()) {
                final ElementNode fpE = addElement(depE, Element.ORIGIN.getLocalName(), ns);
                addAttribute(fpE, Attribute.NAME, entry.getKey());
                addFeatureGroupIncludeExclude(entry.getValue(), ns, fpE);
            }
        }
        if(dep.hasPackageDeps()) {
            PackageXmlWriter.writePackageDeps(dep, addElement(depE, Element.PACKAGES.getLocalName(), ns));
        }
    }

    static void addFeatureGroupIncludeExclude(FeatureGroupSupport dep, String ns, final ElementNode depE) {
        if(dep.hasExcludedSpecs()) {
            for(SpecId spec : dep.getExcludedSpecs()) {
                final ElementNode excludeE = addElement(depE, Element.EXCLUDE.getLocalName(), ns);
                addAttribute(excludeE, Attribute.SPEC, spec.toString());
            }
        }
        if(dep.hasExcludedFeatures()) {
            for(Map.Entry<FeatureId, String> excluded : dep.getExcludedFeatures().entrySet()) {
                final ElementNode excludeE = addElement(depE, Element.EXCLUDE.getLocalName(), ns);
                addAttribute(excludeE, Attribute.FEATURE_ID, excluded.getKey().toString());
                if(excluded.getValue() != null) {
                    addAttribute(excludeE, Attribute.PARENT_REF, excluded.getValue());
                }
            }
        }
        if(dep.hasIncludedSpecs()) {
            for(SpecId spec : dep.getIncludedSpecs()) {
                final ElementNode includeE = addElement(depE, Element.INCLUDE.getLocalName(), ns);
                addAttribute(includeE, Attribute.SPEC, spec.toString());
            }
        }
        if(dep.hasIncludedFeatures()) {
            for(Map.Entry<FeatureId, FeatureConfig> entry : dep.getIncludedFeatures().entrySet()) {
                final ElementNode includeE = addElement(depE, Element.INCLUDE.getLocalName(), ns);
                addAttribute(includeE, Attribute.FEATURE_ID, entry.getKey().toString());
                final FeatureConfig fc = entry.getValue();
                if(fc.getParentRef() != null) {
                    addAttribute(includeE, Attribute.PARENT_REF, fc.getParentRef());
                }
                if(fc != null) {
                    addFeatureConfigBody(includeE, fc, ns);
                }
            }
        }
    }

    private static void addFeatureConfigBody(ElementNode fcE, FeatureConfig fc, String ns) {
        if(fc.hasFeatureDeps()) {
            for(FeatureDependencySpec depSpec : fc.getFeatureDeps()) {
                final ElementNode depE = addElement(fcE, Element.DEPENDS.getLocalName(), ns);
                if(depSpec.getOrigin() != null) {
                    addAttribute(depE, Attribute.ORIGIN, depSpec.getOrigin());
                }
                addAttribute(depE, Attribute.FEATURE_ID, depSpec.getFeatureId().toString());
                if(depSpec.isInclude()) {
                    addAttribute(depE, Attribute.INCLUDE, TRUE);
                }
            }
        }
        if(fc.hasParams()) {
            for(Map.Entry<String, String> param : fc.getParams().entrySet()) {
                final ElementNode paramE = addElement(fcE, Element.PARAM.getLocalName(), ns);
                addAttribute(paramE, Attribute.NAME, param.getKey());
                addAttribute(paramE, Attribute.VALUE, param.getValue());
            }
        }
        if(fc.hasResetParams()) {
            for(String param : fc.getResetParams()) {
                final ElementNode paramE = addElement(fcE, Element.RESET_PARAM.getLocalName(), ns);
                addAttribute(paramE, Attribute.PARAM, param);
            }
        }
        if(fc.hasUnsetParams()) {
            for(String param : fc.getUnsetParams()) {
                final ElementNode paramE = addElement(fcE, Element.UNSET_PARAM.getLocalName(), ns);
                addAttribute(paramE, Attribute.PARAM, param);
            }
        }
        writeFeatureGroupSpecBody(fcE, fc, ns);
    }

    public static void addFeatureConfig(ElementNode parentE, FeatureConfig fc, String ns) {
        final ElementNode fcE = addElement(parentE, Element.FEATURE.getLocalName(), ns);
        addAttribute(fcE, Attribute.SPEC, fc.getSpecId().toString());
        if(fc.getParentRef() != null) {
            addAttribute(fcE, Attribute.PARENT_REF, fc.getParentRef());
        }
        addFeatureConfigBody(fcE, fc, ns);
    }
}
