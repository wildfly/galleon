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
package org.jboss.galleon.repomanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.repomanager.fs.FsTaskContext;
import org.jboss.galleon.repomanager.fs.FsTaskList;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.xml.PackageXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageBuilder {

    public static PackageBuilder newInstance(FeaturePackBuilder fp, String name) {
        return new PackageBuilder(fp, name);
    }

    private final FeaturePackBuilder fp;
    private boolean isDefault;
    private final PackageSpec.Builder pkg;
    private FsTaskList tasks;

    private PackageBuilder(FeaturePackBuilder fp, String name) {
        this.fp = fp;
        pkg = PackageSpec.builder(name);
    }

    private FsTaskList getTasks() {
        return tasks == null ? tasks = FsTaskList.newList() : tasks;
    }

    public FeaturePackBuilder getFeaturePack() {
        return fp;
    }

    public PackageBuilder setDefault() {
        isDefault = true;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public PackageBuilder addDependency(String pkgName) {
        return addDependency(pkgName, false);
    }

    public PackageBuilder addDependency(String pkgName, boolean optional) {
        this.pkg.addPackageDep(pkgName, optional);
        return this;
    }

    public PackageBuilder addDependency(PackageDependencySpec dep) {
        this.pkg.addPackageDep(dep);
        return this;
    }

    public PackageBuilder addDependency(String fpDepName, PackageDependencySpec dep) {
        this.pkg.addPackageDep(fpDepName, dep);
        return this;
    }

    public PackageBuilder addDependency(String fpDepName, String pkgName) {
        this.pkg.addPackageDep(fpDepName, pkgName);
        return this;
    }

    public PackageBuilder addDependency(String fpDepName, String pkgName, boolean optional) {
        this.pkg.addPackageDep(fpDepName, pkgName, optional);
        return this;
    }

    public PackageBuilder addPath(String relativeTarget, Path src) {
        return addPath(relativeTarget, src, true);
    }

    public PackageBuilder addPath(String relativeTarget, Path src, boolean isContent) {
        getTasks().copy(src, relativeTarget, isContent);
        return this;
    }

    public PackageBuilder addDir(String relativeTarget, Path src, boolean contentOnly) {
        return addDir(relativeTarget, src, contentOnly, true);
    }

    public PackageBuilder addDir(String relativeTarget, Path src, boolean contentOnly, boolean isContent) {
        getTasks().copyDir(src, relativeTarget, contentOnly, isContent);
        return this;
    }
    public PackageBuilder writeContent(String relativeTarget, String content) {
        return writeContent(relativeTarget, content, true);
    }

    public PackageBuilder writeContent(String relativeTarget, String content, boolean isContent) {
        getTasks().write(content, relativeTarget, isContent);
        return this;
    }

    public PackageSpec build(Path fpDir) {
        final PackageSpec pkgSpec = pkg.build();
        final Path pkgDir;
        try {
            pkgDir = LayoutUtils.getPackageDir(fpDir, pkgSpec.getName(), false);
        } catch (ProvisioningDescriptionException e) {
            throw new IllegalStateException(e);
        }
        try {
            Files.createDirectories(pkgDir);
            if(tasks != null && !tasks.isEmpty()) {
                tasks.execute(FsTaskContext.builder().setTargetRoot(pkgDir).build());
            }
            PackageXmlWriter.getInstance().write(pkgSpec, pkgDir.resolve(Constants.PACKAGE_XML));
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException(e);
        }
        return pkgSpec;
    }
}
