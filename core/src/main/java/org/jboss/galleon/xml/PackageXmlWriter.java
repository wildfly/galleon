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

import java.util.Collection;

import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageDepsSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.xml.PackageXmlParser10.Attribute;
import org.jboss.galleon.xml.PackageXmlParser10.Element;
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
        final ElementNode fpElement = addElement(deps, PackageDepsSpecXmlParser.Element.ORIGIN.getLocalName(), ns);
        addAttribute(fpElement, Attribute.NAME, origin);
        for(PackageDependencySpec depSpec : depGroup) {
            writePackageDependency(fpElement, depSpec, ns);
        }
    }

    private static void writePackageDependency(ElementNode deps, PackageDependencySpec depSpec, String ns) {
        final ElementNode depElement = addElement(deps, PackageDepsSpecXmlParser.Element.PACKAGE.getLocalName(), ns);
        addAttribute(depElement, Attribute.NAME, depSpec.getName());
        if(depSpec.isOptional()) {
            addAttribute(depElement, PackageDepsSpecXmlParser.Attribute.OPTIONAL, TRUE);
        }
    }
}
