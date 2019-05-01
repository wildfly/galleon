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
package org.jboss.galleon.universe.maven.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.universe.maven.MavenProducer;
import org.jboss.galleon.universe.maven.MavenUniverse;
import org.jboss.galleon.xml.XmlParsers;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenProducerSpecXmlParser {

    private static final MavenProducerSpecXmlParser INSTANCE = new MavenProducerSpecXmlParser();

    public static MavenProducerSpecXmlParser getInstance() {
        return INSTANCE;
    }

    private MavenProducerSpecXmlParser() {
        XmlParsers.getInstance().plugin(MavenProducerSpecXmlParser10.ROOT, new MavenProducerSpecXmlParser10());
    }

    public void parse(final Reader input, final ParsedCallbackHandler<MavenUniverse, MavenProducer> builder) throws XMLStreamException {
        XmlParsers.parse(input, builder);
    }
}
