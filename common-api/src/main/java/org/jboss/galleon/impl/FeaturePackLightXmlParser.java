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
package org.jboss.galleon.impl;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.GalleonFeaturePackDescription;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class FeaturePackLightXmlParser {

    private static final String DEPENDENCIES = "dependencies";
    private static final String DEPENDENCY = "dependency";
    private static final String TRANSITIVE = "transitive";

    public static GalleonFeaturePackDescription parseDescription(Path featurePack) throws ProvisioningException {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(featurePack.toFile())) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(fileInputStream);
                Element root = document.getDocumentElement();
                String producer = root.getAttribute("location");
                String version = root.getAttribute("galleon-min-version");
                List<FPID> dependencies = new ArrayList<>();
                List<FPID> transitives = new ArrayList<>();
                NodeList lst = root.getChildNodes();
                for (int i = 0; i < lst.getLength(); i++) {
                    Node n = lst.item(i);
                    if (n instanceof Element) {
                        if (DEPENDENCIES.equals(n.getNodeName())) {
                            Element e = (Element) n;
                            NodeList deps = e.getChildNodes();
                            for (int j = 0; j < deps.getLength(); j++) {
                                Node dep = deps.item(j);
                                if (dep instanceof Element) {
                                    if (DEPENDENCY.equals(dep.getNodeName())) {
                                        Element depElement = (Element) dep;
                                        String location = depElement.getAttribute("location");
                                        dependencies.add(FeaturePackLocation.fromString(location).getFPID());
                                    }
                                }
                            }
                        } else {
                            if (TRANSITIVE.equals(n.getNodeName())) {
                                Element e = (Element) n;
                                NodeList deps = e.getChildNodes();
                                for (int j = 0; j < deps.getLength(); j++) {
                                    Node dep = deps.item(j);
                                    if (dep instanceof Element) {
                                        if (DEPENDENCY.equals(dep.getNodeName())) {
                                            Element depElement = (Element) dep;
                                            String location = depElement.getAttribute("location");
                                            dependencies.add(FeaturePackLocation.fromString(location).getFPID());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return new GalleonFeaturePackDescription(FeaturePackLocation.fromString(producer).getFPID(), dependencies, transitives, version);
            }
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }

    public static String parseVersion(Path featurePack) throws ProvisioningException {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(featurePack.toFile())) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(fileInputStream);
                Element root = document.getDocumentElement();
                String version = root.getAttribute("galleon-min-version");
                return version;
            }
        } catch (Exception ex) {
            throw new ProvisioningException(ex);
        }
    }
}
