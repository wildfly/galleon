/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.config.FeatureConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureConfigXmlParser implements XmlParser<FeatureConfig> {

    private static final FeatureConfigXmlParser INSTANCE = new FeatureConfigXmlParser();

    public static FeatureConfigXmlParser getInstance() {
        return INSTANCE;
    }

    private FeatureConfigXmlParser() {
    }

    @Override
    public FeatureConfig parse(final Reader input) throws XMLStreamException {
        final FeatureConfig config = new FeatureConfig();
        XmlParsers.parse(input, config);
        return config;
    }
}
