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


import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.diff.ProvisioningDiffResult;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiffResultParser implements XmlParser<ProvisioningDiffResult> {
    public static final String NAMESPACE_1_0 = "urn:jboss:galleon:diff-result:1.0";

    enum Element implements XmlNameProvider {
        ADDED("added-files"),
        CHANGE("diff"),
        CHANGES("unified-diffs"),
        DELETED("deleted-files"),
        DIFF_RESULT("diff-result"),
        MODIFIED("modified-files"),
        PATH("path"),

        // default unknown element
        UNKNOWN(null);

        private static final Map<QName, Element> elements;

        static {
            elements = new HashMap<>(8);
            elements.put(new QName(NAMESPACE_1_0, ADDED.name), ADDED);
            elements.put(new QName(NAMESPACE_1_0, CHANGE.name), CHANGE);
            elements.put(new QName(NAMESPACE_1_0, CHANGES.name), CHANGES);
            elements.put(new QName(NAMESPACE_1_0, DELETED.name), DELETED);
            elements.put(new QName(NAMESPACE_1_0, DIFF_RESULT.name), DIFF_RESULT);
            elements.put(new QName(NAMESPACE_1_0, MODIFIED.name), MODIFIED);
            elements.put(new QName(NAMESPACE_1_0, PATH.name), PATH);
            elements.put(null, UNKNOWN);
        }

        static Element of(QName qName) {
            QName name;
            if (qName.getNamespaceURI().equals("")) {
                name = new QName(NAMESPACE_1_0, qName.getLocalPart());
            } else {
                name = qName;
            }
            final Element element = elements.get(name);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_1_0;

        Element(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        @Override
        public String getLocalName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.DIFF_RESULT.getLocalName());
    private static final FileSystemDiffResultParser INSTANCE = new FileSystemDiffResultParser();

    public static FileSystemDiffResultParser getInstance() {
        return INSTANCE;
    }

    @Override
    public ProvisioningDiffResult parse(Reader input) throws XMLStreamException {
        final ProvisioningDiffResult result = ProvisioningDiffResult.empty();
        XmlParsers.parse(input, result);
        return result;
    }

}
