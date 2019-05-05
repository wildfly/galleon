/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.config;

import org.jboss.galleon.cli.config.mvn.MavenConfigXml;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.ParsingUtils;

import org.jboss.galleon.xml.PlugableXmlParser;
import org.jboss.staxmapper.XMLExtendedStreamReader;

class ConfigXmlParser10 implements PlugableXmlParser<Configuration> {

    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:cli:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, "config");

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, Configuration t) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    // DONE.
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    switch (reader.getLocalName()) {
                        case MavenConfigXml.MAVEN: {
                            try {
                                MavenConfigXml.read(reader, t.getMavenConfig());
                            } catch (ProvisioningException | IOException ex) {
                                throw new XMLStreamException(ex);
                            }
                            break;
                        }
                        default: {
                            throw ParsingUtils.unexpectedContent(reader);
                        }
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
    }
}
