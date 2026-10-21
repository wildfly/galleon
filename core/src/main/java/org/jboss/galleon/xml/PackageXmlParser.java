/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.spec.PackageSpec.Builder;


/**
 *
 * @author Alexey Loubyansky
 */
public class PackageXmlParser implements XmlParser<PackageSpec> {

    private static final PackageXmlParser INSTANCE = new PackageXmlParser();

    public static PackageXmlParser getInstance() {
        return INSTANCE;
    }

    private PackageXmlParser() {
    }

    @Override
    public PackageSpec parse(final Reader input) throws XMLStreamException {
        final Builder pkgBuilder = PackageSpec.builder();
        XmlParsers.parse(input, pkgBuilder);
        return pkgBuilder.build();
    }
}
