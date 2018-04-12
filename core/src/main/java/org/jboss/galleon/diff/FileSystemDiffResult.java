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
package org.jboss.galleon.diff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.repomanager.FeaturePackBuilder;
import org.jboss.galleon.repomanager.PackageBuilder;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.xml.FileSystemDiffResultWriter;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiffResult {

    private final Set<Path> deletedFiles;
    private final Set<Path> addedFiles;
    private final Set<Path> modifiedBinaryFiles;
    private final Map<Path, List<String>> unifiedDiffs;

    public FileSystemDiffResult(Set<Path> deletedFiles, Set<Path> addedFiles, Set<Path> modifiedBinaryFiles, Map<Path, List<String>> unifiedDiffs) {
        this.deletedFiles = new LinkedHashSet<>(deletedFiles);
        this.addedFiles = new LinkedHashSet<>(addedFiles);
        this.modifiedBinaryFiles = new LinkedHashSet<>(modifiedBinaryFiles);
        this.unifiedDiffs = new LinkedHashMap<>(unifiedDiffs);
    }

    public static FileSystemDiffResult empty() {
        return new FileSystemDiffResult(new LinkedHashSet<>(0), new LinkedHashSet<>(0), new LinkedHashSet<>(0), new LinkedHashMap<>(0));
    }

    public Set<Path> getDeletedFiles() {
        return deletedFiles;
    }

    public Set<Path> getAddedFiles() {
        return addedFiles;
    }

    public Map<Path, List<String>> getUnifiedDiffs() {
        return unifiedDiffs;
    }

    public Set<Path> getModifiedBinaryFiles() {
        return modifiedBinaryFiles;
    }

    public FileSystemDiffResult merge(FileSystemDiffResult result) {
        //TODO we might need a better merge strategy
        this.deletedFiles.addAll(result.getDeletedFiles());
        this.addedFiles.addAll(result.getAddedFiles());
        this.modifiedBinaryFiles.addAll(result.getModifiedBinaryFiles());
        this.unifiedDiffs.putAll(result.getUnifiedDiffs());
        return this;
    }

    public void toFeaturePack(FeaturePackBuilder fpBuilder, Map<String, FeaturePackConfig.Builder> builders, ProvisioningRuntime runtime, Path installationHome) throws ProvisioningException {
        PackageBuilder addedFilesPackage = fpBuilder.newPackage("added_files").setDefault();
        for (Path src : getAddedFiles()) {
            addedFilesPackage.addPath(src.toString(), installationHome.resolve(src));
        }
        PackageBuilder updatedFilesPackage = fpBuilder.newPackage("modified_files").setDefault();
        for (Path src : getModifiedBinaryFiles()) {
            updatedFilesPackage.addPath(src.toString(), installationHome.resolve(src));
        }
        for (Path src : getUnifiedDiffs().keySet()) {
            updatedFilesPackage.addPath(src.toString(), installationHome.resolve(src));
        }
    }

    public void toXML(Path target, Path customizedInstallation) throws XMLStreamException, IOException {
        FileSystemDiffResultWriter.getInstance().write(this, target.resolve("filesystem_changes.xml"));
        Path addedDir = target.resolve("added_files");
        for (Path addedFile : getAddedFiles()) {
            Path newFile = addedDir.resolve(addedFile);
            Files.createDirectories(newFile.getParent());
            Files.copy(customizedInstallation.resolve(addedFile), newFile);
        }
        Path modifiedDir = target.resolve("modified_files");
        for (Path modifiedFile : getModifiedBinaryFiles()) {
            Path newFile = modifiedDir.resolve(modifiedFile);
            Files.createDirectories(newFile.getParent());
            Files.copy(customizedInstallation.resolve(modifiedFile), newFile);
        }
    }
}
