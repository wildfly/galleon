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
package org.jboss.galleon.layout;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.xml.PackageXmlParser;
import org.jboss.galleon.xml.XmlParsers;

/**
 * Builds a layout description by analyzing the feature-pack layout
 * structure and parsing included XML files.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLayoutDescriber {

    public static ProvisioningLayout describe(Path fpLayout, String encoding) throws ProvisioningDescriptionException {
        if(!Files.exists(fpLayout)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpLayout));
        }
        if(!Files.isDirectory(fpLayout)) {
            throw new UnsupportedOperationException(); // TODO
        }

        final ProvisioningLayout.Builder layoutBuilder = ProvisioningLayout.builder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fpLayout)) {
            for(Path packageDir : stream) {
                processGroup(layoutBuilder, packageDir, encoding);
            }
        } catch (IOException e) {
            failedToReadDirectory(fpLayout, e);
        }
        return layoutBuilder.build();
    }

    private static void processGroup(final ProvisioningLayout.Builder layoutBuilder, Path groupDir, String encoding) throws ProvisioningDescriptionException {
        assertDirectory(groupDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(groupDir)) {
            for(Path artifactDir : stream) {
                processArtifact(layoutBuilder, artifactDir, encoding);
            }
        } catch (IOException e) {
            failedToReadDirectory(groupDir, e);
        }
    }

    private static void processArtifact(final ProvisioningLayout.Builder layoutBuilder, Path artifactDir, String encoding) throws ProvisioningDescriptionException {
        assertDirectory(artifactDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artifactDir)) {
            for(Path p : stream) {
                layoutBuilder.addFeaturePack(describeFeaturePack(p, encoding));
            }
        } catch (IOException e) {
            failedToReadDirectory(artifactDir, e);
        }
    }

    public static FeaturePackLayout describeFeaturePackZip(Path artifactZip) throws IOException, ProvisioningDescriptionException {
        try (FileSystem zipfs = FileSystems.newFileSystem(artifactZip, null)) {
            for(Path zipRoot : zipfs.getRootDirectories()) {
                return describeFeaturePack(zipRoot, "UTF-8");
            }
        }
        return null;
    }

    public static FeaturePackLayout describeFeaturePack(Path fpDir, String encoding) throws ProvisioningDescriptionException {
        assertDirectory(fpDir);
        final Path fpXml = fpDir.resolve(Constants.FEATURE_PACK_XML);
        if(!Files.exists(fpXml)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(fpXml));
        }
        final FeaturePackLayout.Builder layoutBuilder;
        try (Reader is = Files.newBufferedReader(fpXml, Charset.forName(encoding))) {
            final FeaturePackSpec.Builder specBuilder = FeaturePackSpec.builder();
            XmlParsers.parse(is, specBuilder);
            layoutBuilder = FeaturePackLayout.builder(specBuilder);
        } catch (IOException e) {
            throw new ProvisioningDescriptionException(Errors.openFile(fpXml));
        } catch (XMLStreamException e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(fpXml), e);
        }

        final Path packagesDir = fpDir.resolve(Constants.PACKAGES);
        if(Files.exists(packagesDir)) {
            processPackages(layoutBuilder, packagesDir, encoding);
        }
        return layoutBuilder.build();
    }

    private static void processPackages(FeaturePackLayout.Builder fpBuilder, Path packagesDir, String encoding) throws ProvisioningDescriptionException {
        assertDirectory(packagesDir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packagesDir)) {
            for(Path path : stream) {
                fpBuilder.addPackage(processPackage(path, encoding));
            }
        } catch (IOException e) {
            failedToReadDirectory(packagesDir, e);
        }
    }

    private static PackageSpec processPackage(Path pkgDir, String encoding) throws ProvisioningDescriptionException {
        assertDirectory(pkgDir);
        final Path pkgXml = pkgDir.resolve(Constants.PACKAGE_XML);
        if(!Files.exists(pkgXml)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(pkgXml));
        }
        try (Reader in = Files.newBufferedReader(pkgXml, Charset.forName(encoding))) {
            return PackageXmlParser.getInstance().parse(in);
        } catch (IOException e) {
            throw new ProvisioningDescriptionException(Errors.openFile(pkgXml), e);
        } catch (XMLStreamException e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(pkgXml), e);
        }
    }

    private static void assertDirectory(Path dir) throws ProvisioningDescriptionException {
        if(!Files.isDirectory(dir)) {
            throw new ProvisioningDescriptionException(Errors.notADir(dir));
        }
    }

    private static void failedToReadDirectory(Path p, IOException e) throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException(Errors.readDirectory(p), e);
    }
}
