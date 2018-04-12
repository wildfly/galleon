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
package org.jboss.galleon.featurepack.pkg.xml.test;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.PackageXmlParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class PackageXmlParserTestCase {

    private static final XmlParserValidator<PackageSpec> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/pm-package-1_0.xsd"), PackageXmlParser.getInstance());

    private static final Locale defaultLocale = Locale.getDefault();

    @BeforeClass
    public static void setLocale() {
        Locale.setDefault(Locale.US);
    }
    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void readBadNamespace() throws Exception {
        /*
         * urn:wildfly:pm-provisioning:1.0.1 used in provisioning-1.0.1.xml is not registered in ProvisioningXmlParser
         */
        validator.validateAndParse("xml/package/package-1.0.1.xml",
                "Cannot find the declaration of element 'package-spec'.",
                "Message: Unexpected element '{urn:wildfly:pm-package:1.0.1}package-spec'");
    }

    @Test
    public void readMissingPackageName() throws Exception {
        validator.validateAndParse("xml/package/package-1.0-missing-package-name.xml",
                "cvc-complex-type.4: Attribute 'name' must appear on element 'package-spec'.",
                "Message: Missing required attributes name");
    }

    @Test
    public void readMissingDependencyName() throws Exception {
        validator.validateAndParse("xml/package/package-1.0-missing-dependency-name.xml",
                "cvc-complex-type.4: Attribute 'name' must appear on element 'package'.",
                "Message: Missing required attributes name");
    }

    @Test
    public void readEmptyDependencies() throws Exception {
        validator.validateAndParse("xml/package/package-1.0-empty-dependencies.xml",
                "cvc-complex-type.2.4.b: The content of element 'dependencies' is not complete. One of '{\"urn:wildfly:pm-package:1.0\":package, \"urn:wildfly:pm-package:1.0\":origin}' is expected.",
                "The content of element 'dependencies' is not complete. One of 'package', 'origin' is expected.");
    }

    @Test
    public void readMissingDependencies() throws Exception {
        final PackageSpec parsedPkg = validator.validateAndParse("xml/package/package-1.0-missing-dependencies.xml", null, null);
        Assert.assertEquals(PackageSpec.forName("package1"), parsedPkg);
    }

    @Test
    public void readValid() throws Exception {
        PackageSpec found = validator.validateAndParse("xml/package/package-1.0.xml", null, null);
        PackageSpec expected = PackageSpec.builder("package1")
                .addPackageDep("dep1")
                .addPackageDep("dep2")
                .addPackageDep("fp-dep", "dep1")
                .addPackageDep("fp-dep", "dep2")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void readOptionalDependencies() throws Exception {
        PackageSpec found = validator.validateAndParse("xml/package/package-1.0-optional-dependencies.xml", null, null);
        PackageSpec expected = PackageSpec.builder("package1")
                .addPackageDep("dep1")
                .addPackageDep("dep2")
                .addPackageDep("dep3", true)
                .build();
        Assert.assertEquals(expected, found);
    }
}
