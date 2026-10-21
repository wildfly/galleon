/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.xml;

import java.util.Collection;

import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageDepsSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.xml.PackageXmlParser30.Attribute;
import org.jboss.galleon.xml.PackageXmlParser30.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageXmlWriter extends BaseXmlWriter<PackageSpec> {

    private static final String TRUE = "true";

    private static final PackageXmlWriter INSTANCE = new PackageXmlWriter();

    public static PackageXmlWriter getInstance() {
        return INSTANCE;
    }

    private PackageXmlWriter() {
    }

    protected ElementNode toElement(PackageSpec pkgSpec) {

        final ElementNode pkg = addElement(null, Element.PACKAGE_SPEC);
        addAttribute(pkg, Attribute.NAME, pkgSpec.getName());
        if (pkgSpec.getStability() != null) {
            addAttribute(pkg, Attribute.STABILITY_LEVEL, pkgSpec.getStability().toString());
        }
        if(pkgSpec.hasPackageDeps()) {
            writePackageDeps(pkgSpec, addElement(pkg, Element.DEPENDENCIES.getLocalName(), Element.DEPENDENCIES.getNamespace()));
        }

        return pkg;
    }

    static void writePackageDeps(PackageDepsSpec pkgDeps, ElementNode deps) {
        if(pkgDeps.hasLocalPackageDeps()) {
            for(PackageDependencySpec depSpec : pkgDeps.getLocalPackageDeps()) {
                writePackageDependency(deps, depSpec, deps.getNamespace());
            }
        }
        if(pkgDeps.hasExternalPackageDeps()) {
            for(String origin : pkgDeps.getPackageOrigins()) {
                writeOrigin(deps, origin, pkgDeps.getExternalPackageDeps(origin), deps.getNamespace());
            }
        }
    }

    private static void writeOrigin(ElementNode deps, String origin, Collection<PackageDependencySpec> depGroup, String ns) {
        final ElementNode fpElement = addElement(deps, PackageDepsSpecXmlParser.ORIGIN, ns);
        addAttribute(fpElement, Attribute.NAME, origin);
        for(PackageDependencySpec depSpec : depGroup) {
            writePackageDependency(fpElement, depSpec, ns);
        }
    }

    private static void writePackageDependency(ElementNode deps, PackageDependencySpec depSpec, String ns) {
        final ElementNode depElement = addElement(deps, PackageDepsSpecXmlParser.PACKAGE, ns);
        addAttribute(depElement, Attribute.NAME, depSpec.getName());
        if(depSpec.isOptional()) {
            if(depSpec.isPassive()) {
                addAttribute(depElement, PackageDepsSpecXmlParser.Attribute.PASSIVE, TRUE);
            } else {
                addAttribute(depElement, PackageDepsSpecXmlParser.Attribute.OPTIONAL, TRUE);
            }
            if (depSpec.getValidForStability() != null) {
                addAttribute(depElement, PackageDepsSpecXmlParser.Attribute.VALID_FOR_STABILITY, depSpec.getValidForStability());
            }
        }
    }
}
