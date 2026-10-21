/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import java.io.IOException;
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
import java.util.Objects;
import java.util.Set;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.PackageSpec;

import org.jboss.galleon.plugin.CliPlugin;
import org.jboss.galleon.plugin.CliPlugin.CustomPackageContent;
import org.jboss.galleon.runtime.FeaturePackRuntime;
import org.jboss.galleon.runtime.PackageRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public class PackageInfo {

    private final Path contentDir;
    private final PackageSpec spec;
    private final List<String> content = new ArrayList<>();

    private final Identity identity;

    private Set<Identity> providers = new HashSet<>();
    private final FeaturePackLocation.FPID fpid;
    private final CustomPackageContent customContent;

    private final PackageRuntime pkg;
    PackageInfo(PackageRuntime pkg, Identity identity,
            CliPlugin plugin) throws IOException, ProvisioningException {
        this.pkg = pkg;
        this.fpid = pkg.getFeaturePackRuntime().getFPID();
        this.identity = identity;
        this.contentDir = pkg.getContentDir();
        this.spec = pkg.getSpec();
        customContent = plugin == null ? null : plugin.handlePackageContent(pkg);
        if (customContent == null) {
            fillContent();
        }
    }

    FeaturePackRuntime getFeaturePackRuntime() {
        return pkg.getFeaturePackRuntime();
    }

    public String getCustomContent() {
        if (customContent == null) {
            return null;
        }
        return customContent.getInfo();
    }

    public FeaturePackLocation.FPID getFPID() {
        return fpid;
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

    public List<String> getContent() {
        return content;
    }

    /**
     * @return the name
     */
    public Identity getIdentity() {
        return identity;
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
