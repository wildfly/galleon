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
package org.jboss.galleon.cli.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.spec.PackageSpec;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

/**
 *
 * @author jdenise@redhat.com
 */
public class PackageInfo {

    private static String MODULE_PATH = "pm/wildfly/module";
    private static String MODULE_XML = "module.xml";

    private final Path contentDir;
    private final PackageSpec spec;

    private final List<String> artifacts = new ArrayList<>();
    private String moduleVersion;
    private boolean isModule;
    private boolean hasContent;
    List<String> content = new ArrayList<>();

    private final Identity identity;

    private Set<Identity> providers = new HashSet<>();
    private final Gav gav;

    PackageInfo(Gav gav, Identity identity, Path contentDir, PackageSpec spec,
            Map<String, String> variables) throws IOException, ProvisioningException {
        this.gav = gav;
        this.identity = identity;
        this.contentDir = contentDir;
        this.spec = spec;
        Path modulePath = contentDir.getParent().resolve(MODULE_PATH);
        isModule = Files.exists(modulePath);
        hasContent = Files.exists(contentDir);
        if (isModule()) {
            try {
                parseModuleDescriptor(variables);
            } catch (ParsingException ex) {
                throw new ProvisioningException(ex);
            }
        } else if (hasContent()) {
            fillContent();
        }
    }

    public Gav getGav() {
        return gav;
    }

    private void fillContent() throws IOException {
        Files.walkFileTree(contentDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attr) {
                String r = contentDir.toString();
                String child = file.toString().substring(0, file.toString().length());
                String name = child.substring(r.length() + 1);
                content.add(name);
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                    IOException exc) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file,
                    IOException exc) {
                return CONTINUE;
            }
        });
        Collections.sort(content);
    }

    private void parseModuleDescriptor(Map<String, String> variables) throws IOException, ProvisioningException, ParsingException {
        Path modulePath = contentDir.getParent().resolve(MODULE_PATH);
        List<Path> moduleHolder = new ArrayList<>();
        Files.walkFileTree(modulePath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attr) {
                if (file.getFileName().toString().equals(MODULE_XML)) {
                    moduleHolder.add(file);
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                    IOException exc) throws IOException {

                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file,
                    IOException exc) {
                return CONTINUE;
            }
        });
        if (moduleHolder.isEmpty()) {
            throw new ProvisioningException("No module descriptor for " + getIdentity());
        }
        Path p = moduleHolder.get(0);
        final Builder builder = new Builder(false);
        final Document document;
        try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            document = builder.build(reader);
        }
        final Element rootElement = document.getRootElement();
        final Attribute versionAttribute = rootElement.getAttribute("version");
        if (versionAttribute != null) {
            final String versionExpr = versionAttribute.getValue();
            if (versionExpr.startsWith("${") && versionExpr.endsWith("}")) {
                final String exprBody = versionExpr.substring(2, versionExpr.length() - 1);
                final int optionsIndex = exprBody.indexOf('?');
                final String artifactName;
                if (optionsIndex > 0) {
                    artifactName = exprBody.substring(0, optionsIndex);
                } else {
                    artifactName = exprBody;
                }
                String vers = variables.get(artifactName);
                if (vers != null) {
                    int i = vers.lastIndexOf(":");
                    if (i > 0) {
                        vers = vers.substring(i + 1);
                    }
                    moduleVersion = vers;
                }
            }
        }
        final Element resourcesElement = rootElement.getFirstChildElement("resources", rootElement.getNamespaceURI());
        if (resourcesElement != null) {
            final Elements artfs = resourcesElement.getChildElements("artifact", rootElement.getNamespaceURI());
            final int artifactCount = artfs.size();
            for (int i = 0; i < artifactCount; i++) {
                final Element element = artfs.get(i);
                assert element.getLocalName().equals("artifact");
                final Attribute attribute = element.getAttribute("name");
                final String nameExpr = attribute.getValue();
                if (nameExpr.startsWith("${") && nameExpr.endsWith("}")) {
                    final String exprBody = nameExpr.substring(2, nameExpr.length() - 1);
                    final int optionsIndex = exprBody.indexOf('?');
                    final String artifactName;
                    if (optionsIndex >= 0) {
                        artifactName = exprBody.substring(0, optionsIndex);
                    } else {
                        artifactName = exprBody;
                    }
                    final String resolved = variables.get(artifactName);
                    this.artifacts.add(resolved);
                }
            }
        }
    }

    public List<String> getContent() {
        return content;
    }

    /**
     * @return the name
     */
    public Identity getIdentity() {
        return identity;
    }

    public final boolean isModule() {
        return isModule;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public final boolean hasContent() {
        return hasContent;
    }

    /**
     * @return the spec
     */
    public PackageSpec getSpec() {
        return spec;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageInfo)) {
            return false;
        }
        PackageInfo pi = (PackageInfo) obj;
        return pi.getIdentity().equals(getIdentity());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.identity);
        return hash;
    }

    void addProvider(Identity provider) {
        providers.add(provider);
    }

    public Set<Identity> getProviders() {
        return providers;
    }

}
