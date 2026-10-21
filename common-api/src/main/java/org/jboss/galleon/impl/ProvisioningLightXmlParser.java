/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ProvisioningLightXmlParser {

    private static final String FEATURE_PACK = "feature-pack";
    private static final String UNIVERSES = "universes";
    private static final String UNIVERSE = "universe";
    private static final String DEFAULT_UNIVERSE = "";
    public static List<FPID> parse(Path configFile) throws ProvisioningException {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(configFile.toFile())) {
                return parse(fileInputStream);
            }
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }

    public static List<FPID> parse(InputStream fileInputStream) throws ProvisioningException {
        List<FPID> featurePacks = new ArrayList<>();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(fileInputStream);
            Element root = document.getDocumentElement();

            NodeList lst = root.getChildNodes();
            Map<String, UniverseSpec> universes = new HashMap<>();
            for (int i = 0; i < lst.getLength(); i++) {
                Node n = lst.item(i);
                if (n instanceof Element) {
                    if (UNIVERSES.equals(n.getNodeName())) {
                        Element e = (Element) n;
                        NodeList lstUniverses = e.getChildNodes();
                        for (int j = 0; j < lstUniverses.getLength(); j++) {
                            Node u = lstUniverses.item(j);
                            if (u instanceof Element) {
                                if (UNIVERSE.equals(u.getNodeName())) {
                                    Element uElement = (Element) u;
                                    String name = uElement.getAttribute("name");
                                    // Default universe
                                    if (name == null) {
                                        name = DEFAULT_UNIVERSE;
                                    }
                                    String factory = uElement.getAttribute("factory");
                                    String loc = uElement.getAttribute("location");
                                    UniverseSpec spec = new UniverseSpec(factory, loc);
                                    universes.put(name, spec);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < lst.getLength(); i++) {
                Node n = lst.item(i);
                if (n instanceof Element) {
                    if (FEATURE_PACK.equals(n.getNodeName())) {
                        Element e = (Element) n;
                        String loc = e.getAttribute("location");
                        FeaturePackLocation location = FeaturePackLocation.fromString(loc);
                        UniverseSpec spec = null;
                        if (location.getUniverse() == null) {
                            spec = universes.get(DEFAULT_UNIVERSE);
                        } else {
                            if (location.getUniverse().getLocation() == null) {
                                spec = universes.get(location.getUniverse().getFactory());
                            }
                        }
                        if (spec != null) {
                            FeaturePackLocation l = new FeaturePackLocation(spec,
                                    location.getProducerName(), location.getChannelName(),
                                    location.getFrequency(), location.getBuild());
                            location = l;
                        }
                        featurePacks.add(location.getFPID());
                    }
                }
            }
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
        return featurePacks;
    }
}
