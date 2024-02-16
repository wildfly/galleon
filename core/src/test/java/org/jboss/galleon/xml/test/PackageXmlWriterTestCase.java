/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.xml.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.xml.PackageXmlParser;
import org.jboss.galleon.xml.PackageXmlWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Bartosz Spyrko-Smietanko (bspyrkos@redhat.com)
 */
public class PackageXmlWriterTestCase {
    private static final String SCHEMA = "schema/galleon-package-3_0.xsd";

    private static XmlParserValidator<PackageSpec> validator;
    private static Path tmpDir;


    @BeforeClass
    public static void before() throws Exception {
        URL xsd = PackageXmlWriterTestCase.class.getClassLoader().getResource(SCHEMA);
        validator = new XmlParserValidator<PackageSpec>(Paths.get(xsd.toURI()), PackageXmlParser.getInstance());
        tmpDir = IoUtils.createRandomTmpDir();
    }

    @AfterClass
    public static void after() throws Exception {
        IoUtils.recursiveDelete(tmpDir);
    }

    @Test
    public void testMarshallUnmarshall() throws Exception {
        PackageSpec originalState = PackageSpec.builder()
                .setName("test-package")
                .addPackageDep("test-dep")
                .addPackageDep("optional-dep", true)
                .addPackageDep("external", "external-dep")
                .addPackageDep("external", "external-optional-dep", true)
                .addPackageDep(PackageDependencySpec.required("pkg-spec-dep"))
                .addPackageDep(PackageDependencySpec.optional("pkg-spec-optional-dep"))
                .addPackageDep("external", PackageDependencySpec.required("pkg-spec-external-dep"))
                .build();

        Path path = marshallToTempFile(originalState);
        PackageSpec newState = validator.validateAndParse(path);

        assertEquals(originalState, newState);
    }

    @Test
    public void testAddMultiplePackageDepsWithSameName() throws Exception {
        PackageSpec originalState = PackageSpec.builder()
                .setName("test-package")
                .addPackageDep("test-dep")
                .addPackageDep("test-dep", true)
                .build();

        Path path = marshallToTempFile(originalState);
        PackageSpec newState = validator.validateAndParse(path);

        assertEquals(originalState, newState);
        assertTrue(firstPackage(newState).isOptional());
    }

    @Test
    public void testAddMultiplePackageDepsInSameOriginWithSameName() throws Exception {
        PackageSpec originalState = PackageSpec.builder()
                .setName("test-package")
                .addPackageDep("external", "test-dep")
                .addPackageDep("external", "test-dep", true)
                .build();

        Path path = marshallToTempFile(originalState);
        PackageSpec newState = validator.validateAndParse(path);

        assertEquals(originalState, newState);
        assertTrue(firstPackage(newState,"external").isOptional());
    }

    @Test
    public void testAddMultipleOrigins() throws Exception {
        PackageSpec originalState = PackageSpec.builder()
                .setName("test-package")
                .addPackageDep("external1", "test-dep")
                .addPackageDep("external2", "test-dep")
                .addPackageDep("external2", "test-dep", true)
                .build();

        Path path = marshallToTempFile(originalState);
        PackageSpec newState = validator.validateAndParse(path);

        assertEquals(originalState, newState);
    }

    @Test
    public void testOrderOfPackageDeps() throws Exception {
        PackageSpec originalState = PackageSpec.builder()
                .setName("test-package")
                .addPackageDep("test-dep")
                .addPackageDep("test-dep2")
                .addPackageDep("test-dep3")
                .build();

        Path path = marshallToTempFile(originalState);
        PackageSpec newState = validator.validateAndParse(path);

        assertEquals(originalState, newState);
        Iterator<PackageDependencySpec> depsIterator = newState.getLocalPackageDeps().iterator();
        assertEquals("test-dep", depsIterator.next().getName());
        assertEquals("test-dep2", depsIterator.next().getName());
        assertEquals("test-dep3", depsIterator.next().getName());
    }

    private Path marshallToTempFile(PackageSpec state) throws Exception {
        final Path path = tmpDir.resolve("test-package.xml");
        PackageXmlWriter.getInstance().write(state, path);
        return path;
    }

    private PackageDependencySpec firstPackage(PackageSpec newState, String origin) {
        if (origin == null) {
            return newState.getLocalPackageDeps().iterator().next();
        } else {
            return newState.getExternalPackageDeps("external").iterator().next();
        }
    }

    private PackageDependencySpec firstPackage(PackageSpec newState) {
        return firstPackage(newState, null);
    }
}
