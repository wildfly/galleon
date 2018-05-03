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
package org.jboss.galleon.cli.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.xml.FeatureSpecXmlParser;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureSpecsBuilder {

    private final Map<ResolvedSpecId, FeatureSpecInfo> allspecs = new HashMap<>();

    public Map<ResolvedSpecId, FeatureSpecInfo> getAllSpecs() {
        return allspecs;
    }

    public Group buildTree(PmSession session, ArtifactCoords.Gav fpgav,
            ArtifactCoords.Gav gav,
            Map<Identity, Group> allPackages, boolean useCache, Set<ResolvedSpecId> wantedSpecs) throws IOException, ArtifactException {
        // Build the tree of specs located in all feature-packs
        FeatureGroupsBuilder grpBuilder = new FeatureGroupsBuilder();

        // Do we have feature-specs in cache?
        Set<FeatureSpecInfo> specs = null;
        Map<Gav, Set<FeatureSpecInfo>> allSpecs = null;
        if (useCache) {
            allSpecs = Caches.getSpecs();
            if (allSpecs != null) {
                specs = allSpecs.get(fpgav);
            }
        }
        if (specs == null) {
            specs = new HashSet<>();
            final Set<FeatureSpecInfo> fSpecs = specs;
            FileSystem fs = FileSystems.newFileSystem(session.getArtifactResolver().resolve(fpgav.toArtifactCoords()), null);
            try {
                final Path path = fs.getPath("features/");
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attr) {
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir,
                            IOException exc) throws IOException {
                        if (!dir.equals(path)) {
                            // Can't use relativize with zip FS.
                            String r = path.toString();
                            String child = dir.toString().substring(0, dir.toString().length() - 1);
                            String name = child.substring(r.length() + 1);
                            ResolvedSpecId resolved = new ResolvedSpecId(fpgav, name);
                            if (wantedSpecs == null || wantedSpecs.contains(resolved)) {
                                FeatureSpecInfo specInfo = allspecs.get(resolved);
                                if (specInfo == null) {
                                    try {
                                        Set<Identity> missingPackages = new HashSet<>();
                                        FeatureSpec spec = getFeatureSpec(fs, name);
                                        specInfo = new FeatureSpecInfo(resolved, gav, spec);
                                        Identity specId = Identity.fromGav(resolved.getGav(), resolved.getName());
                                        boolean featureEnabled = true;
                                        for (PackageDependencySpec p : spec.getLocalPackageDeps()) {
                                            Identity id = Identity.fromGav(resolved.getGav(), p.getName());
                                            Group grp = allPackages.get(id);
                                            // Group can be null if the modules have not been installed.
                                            if (grp != null) {
                                                specInfo.addPackage(grp.getPackage());
                                                attachProvider(specId, grp, new HashSet<>());
                                            } else {
                                                featureEnabled = false;
                                                missingPackages.add(id);
                                            }
                                        }
                                        for (String o : spec.getPackageOrigins()) {
                                            for (PackageDependencySpec p : spec.getExternalPackageDeps(o)) {
                                                Identity id = Identity.fromString(o, p.getName());
                                                Group grp = allPackages.get(id);
                                                if (grp != null) {
                                                    specInfo.addPackage(grp.getPackage());
                                                    attachProvider(specId, grp, new HashSet<>());
                                                } else {
                                                    featureEnabled = false;
                                                    missingPackages.add(id);
                                                }
                                            }
                                        }
                                        specInfo.setEnabled(featureEnabled);
                                        specInfo.setMissingPackages(missingPackages);
                                        allspecs.put(resolved, specInfo);
                                        fSpecs.add(specInfo);
                                    } catch (XMLStreamException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }

                                String fullSpecName = resolved.getName();
                                List<String> path = new ArrayList<>();
                                Group parent = grpBuilder.buildFeatureSpecGroups(fullSpecName, specInfo, path);
                                parent.setFeatureSpec(specInfo);
                            }
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file,
                            IOException exc) {
                        return CONTINUE;
                    }
                });
            } finally {
                fs.close();
            }
            if (useCache) {
                if (allSpecs == null) {
                    allSpecs = new HashMap<>();
                    Caches.addSpecs(allSpecs);
                }
                allSpecs.put(fpgav, specs);
            }
        } else {
            for (FeatureSpecInfo spec : specs) {
                allspecs.put(spec.getSpecId(), spec);
                String fullSpecName = spec.getSpecId().getName();
                List<String> path = new ArrayList<>();
                Group parent = grpBuilder.buildFeatureSpecGroups(fullSpecName, spec, path);
                parent.setFeatureSpec(spec);
            }
        }
        return grpBuilder.getRoot();
    }

    private static void attachProvider(Identity provider, Group grp, HashSet<Group> seen) {
        grp.getPackage().addProvider(provider);
        if (seen.contains(grp)) {
            return;
        }
        seen.add(grp);
        for (Group dep : grp.getGroups()) {
            attachProvider(provider, dep, seen);
        }
    }

    private static FeatureSpec getFeatureSpec(FileSystem fs, String name) throws IOException, XMLStreamException {
        final Path path = fs.getPath("features/" + name + "/spec.xml");
        byte[] content = Files.readAllBytes(path);
        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return FeatureSpecXmlParser.getInstance().parse(reader);
        }
    }
}
