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
package org.jboss.galleon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.runtime.ResolvedFeature;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public interface Errors {

    // GENERAL MESSAGES

    static String pathDoesNotExist(Path p) {
        return "Failed to locate " + p.toAbsolutePath();
    }

    static String pathAlreadyExists(Path p) {
        return "Path already exists " + p.toAbsolutePath();
    }

    static String mkdirs(Path p) {
        return "Failed to make directories " + p.toAbsolutePath();
    }

    static String readDirectory(Path p) {
        return "Failed to read directory " + p.toAbsolutePath();
    }

    static String notADir(Path p) {
        return p.toAbsolutePath() + " is not a directory";
    }

    static String copyFile(Path src, Path target) {
        return "Failed to copy " + src + " to " + target;
    }

    static String deletePath(Path src) {
        return "Failed to delete " + src;
    }

    static String moveFile(Path src, Path target) {
        return "Failed to move " + src.toAbsolutePath() + " to " + target.toAbsolutePath();
    }

    static String openFile(Path p) {
        return "Failed to open " + p.toAbsolutePath();
    }

    static String readFile(Path p) {
        return "Failed to read " + p.toAbsolutePath();
    }

    static String parseXml() {
        return "Failed to parse XML";
    }

    static String parseXml(Path p) {
        return "Failed to parse " + p.toAbsolutePath();
    }

    static String writeFile(Path p) {
        return "Failed to write to " + p.toAbsolutePath();
    }

    static String hashCalculation(Path path) {
        return "Hash calculation failed for " + path;
    }

    // FEATURE PACK INSTALL MESSAGES

    static String homeDirNotUsable(Path p) {
        return p + " has to be empty or contain a provisioned installation to be used by the tool";
    }

    static String fpVersionCheckFailed(Collection<ArtifactCoords.Ga> missingVersions, Collection<Set<ArtifactCoords.Gav>> versionConflicts) throws ProvisioningException {
        final StringWriter strWriter = new StringWriter();
        try(BufferedWriter writer = new BufferedWriter(strWriter)) {
            writer.write("Feature-pack versions check failed with the following errors:");
            writer.newLine();

            if (!missingVersions.isEmpty()) {
                writer.write(" * ");
                writer.write(Errors.failedToResolveReleaseVersions(missingVersions));
                writer.write(';');
                writer.newLine();
            }

            if(!versionConflicts.isEmpty()) {
                for (Collection<ArtifactCoords.Gav> entry : versionConflicts) {
                    writer.write(" * ");
                    writer.write(Errors.featurePackVersionConflict(entry));
                    writer.write(';');
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ProvisioningException("Failed to report version check errors", e);
        }
        return strWriter.toString();
    }

    static String failedToResolveReleaseVersions(Collection<ArtifactCoords.Ga> gas) {
        final StringBuilder buf = new StringBuilder("Missing release version");
        if(gas.size() > 1) {
            buf.append('s');
        }
        buf.append(" of ");
        StringUtils.append(buf, gas);
        return buf.toString();
    }

    static String packageContentCopyFailed(String packageName) {
        return "Failed to copy package " + packageName + " content";
    }

    static String packageNotFound(ArtifactCoords.Gav fp, String packageName) {
        return "Failed to resolve package " + packageName + " in " + fp;
    }

    static String unknownPackage(ArtifactCoords.Gav gav, String pkgName) {
        return "Package " + pkgName + " is not found in " + gav;
    }

    static String unknownFeaturePack(ArtifactCoords.Gav gav) {
        return "Feature-pack " + gav + " is not found";
    }

    static String unsatisfiedFeaturePackDep(ArtifactCoords.Gav gav) {
        return "Feature-pack " + gav + " is required dependency";
    }

    static String unknownFeaturePackDependency(ArtifactCoords.Ga ga) {
        return ga + " is not found among the feature-pack dependencies";
    }

    static String featurePackVersionConflict(ArtifactCoords.Gav gav, ArtifactCoords.Gav gav2) {
        final Set<Gav> gavs = new LinkedHashSet<>(2);
        gavs.add(gav);
        gavs.add(gav2);
        return featurePackVersionConflict(gavs);
    }

    static String featurePackVersionConflict(Collection<ArtifactCoords.Gav> gavs) {
        final Iterator<Gav> i = gavs.iterator();
        Gav gav = i.next();
        final StringBuilder buf = new StringBuilder("Please pick the desired version of ")
                .append(gav.toGa())
                .append(" explicitly in the provisioning config. Current configuration references the following versions ")
                .append(gav.getVersion());
        while(i.hasNext()) {
            gav = i.next();
            buf.append(", ").append(gav.getVersion());
        }
        return buf.toString();
    }

    static String unsatisfiedPackageDependencies(ArtifactCoords.Gav fpGav, String packageName, Collection<String> unsatisfiedDeps) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Feature-pack ").append(fpGav).append(" package ").append(packageName).append(" has unsatisfied dependencies on packages: ");
        StringUtils.append(buf, unsatisfiedDeps);
        return buf.toString();
    }

    static String unsatisfiedPackageDependency(ArtifactCoords.Gav fpGav, String targetPackage) {
        return "Unsatisfied dependency on feature-pack " + fpGav + " package " + targetPackage;
    }

    static String unsatisfiedExternalPackageDependency(ArtifactCoords.Gav srcGav, String srcPackage, ArtifactCoords.Gav targetGav, String targetPackage) {
        return "Feature-pack " + srcGav + " package " + srcPackage + " has unsatisfied dependency on feature-pack " + targetGav + " package " + targetPackage;
    }

    static String resolvePackage(ArtifactCoords.Gav fpGav, String packageName) {
        return "Failed to resolve feature-pack " + fpGav + " package " + packageName;
    }

    static String packageExcludeInclude(String packageName) {
        return "Attempt to explicitly include and exclude package " + packageName;
    }

    static String duplicateDependencyName(String name) {
        return "Dependency with name " + name + " already exists";
    }

    static String unknownFeaturePackDependencyName(Gav gav, String depName) {
        return "Dependency " + depName + " not found in " + gav + " feature-pack description";
    }

    static String unknownFeaturePackDependencyName(String depName) {
        return depName + " was not found among the feature-pack dependencies";
    }

    static String featurePackAlreadyInstalled(Gav gav) {
        return "Feature-pack " + gav + " is already installed";
    }

    static String unknownFeaturePackDependencyName(ArtifactCoords.Gav fpGav, String pkgName, String depName) {
        return fpGav + " package " + pkgName + " references unknown feature-pack dependency " + depName;
    }

    static String packageAlreadyExists(Gav gav, String name) {
        return "Package " + name + " already exists in feature-pack " + gav;
    }

    static String noCapabilityProvider(String capability) {
        return "No provider found for capability '" + capability + "'";
    }

    static String noCapabilityProvider(ResolvedFeature feature, CapabilitySpec capSpec, String resolvedCap) {
        final StringBuilder buf = new StringBuilder();
        buf.append("No provider found for capability ").append(resolvedCap);
        buf.append(" required by ");
        if(feature.hasId()) {
            buf.append(feature.getId());
        } else {
            buf.append(" an instance of ").append(feature.getSpecId());
        }
        if(!capSpec.isStatic()) {
            buf.append(" as ").append(capSpec.toString());
        }
        return buf.toString();
    }

    static String capabilityMissingParameter(CapabilitySpec cap, String param) {
        return "Parameter " + param + " is missing value to resolve capability " + cap;
    }

    static String illegalCapabilityElement(CapabilitySpec cap, String resolvedPart, String elem) {
        return "Failed to resolve capability " + cap + " with illegal element value of '" + elem + "' at " + resolvedPart;
    }

    static String failedToResolveCapability(ResolvedFeature feature, CapabilitySpec cap) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to resolve capability ").append(cap).append(" for ");
        appendFeature(buf, feature);
        return buf.toString();
    }

    static String failedToResolveParameter(ResolvedSpecId specId, String name) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to resolve ").append(specId).append(" parameter ").append(name);
        return buf.toString();
    }

    static String failedToResolveParameter(ResolvedSpecId specId, String name, String value) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to resolve ").append(specId).append(" parameter ").append(name).append(" value ").append(value);
        return buf.toString();
    }

    static String failedToResolveFeatureReference(FeatureReferenceSpec refSpec, ResolvedSpecId spec) {
        return "Failed to resolve feature reference " + refSpec.getName() + " for " + spec;
    }

    static String failedToResolveConfigSpec(String model, String name) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to resolve config");
        appendConfig(buf, model, name);
        return buf.toString();
    }

    static String failedToBuildConfigSpec(String model, String name) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to build config");
        appendConfig(buf, model, name);
        return buf.toString();
    }

    static String failedToInitializeForeignKeyParams(ResolvedSpecId specId, ResolvedFeatureId parentId, Map<String, ?> params) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to initialize the foreign key parameters for the feature ").append(specId).append(" with parameters ");
        StringUtils.append(buf, params.entrySet());
        buf.append(" referencing ID ").append(parentId);
        return buf.toString();
    }

    static String idParamForeignKeyInitConflict(ResolvedSpecId specId, String param, Object prevValue, Object newValue) {
        return "Value '" + prevValue + "' of ID parameter " + param + " of " + specId
                + " conflicts with the corresponding parent ID value '" + newValue + "'";
    }

    static String nonExistingForeignKeyTarget(String localParam, String refName, ResolvedSpecId specId, String targetParam, ResolvedSpecId targetSpecId) {
        return "Foreign key parameter " + localParam + " of " + specId + " reference " + refName +
                " is mapped to a non-existing ID parameter " + targetParam + " of " + targetSpecId;
    }

    static String nonExistingForeignKeyParam(String refName, ResolvedSpecId specId, String targetParam) {
        return "Foreign key parameter " + targetParam + " of " + specId + " reference " + refName + " does not exist";
    }

    static String featureRefNotInSpec(String featureRefName, String featureSpec) {
        return "Feature spec " + featureSpec + " does not include a feature reference named " + featureRefName;
    }

    static String nonNillableRefIsNull(ResolvedFeature feature, String refName) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Reference ").append(refName).append(" of ");
        appendFeature(buf, feature);
        return buf.append(" cannot be null").toString();
    }

    static String unresolvedFeatureDep(ResolvedFeature feature, ResolvedFeatureId dep) {
        final StringBuilder buf = new StringBuilder();
        appendFeature(buf, feature);
        buf.append(" has unresolved dependency on ").append(dep);
        return buf.toString();
    }

    static String nonNillableParameterIsNull(ResolvedFeature feature, String paramName) {
        if(feature.hasId()) {
            return nonNillableParameterIsNull(feature.getId(), paramName);
        }
        return nonNillableParameterIsNull(feature.getSpecId(), paramName);
    }

    static String nonNillableParameterIsNull(ResolvedSpecId specId, String paramName) {
        return "Non-nillable parameter " + paramName + " of " + specId + " has not been initialized";
    }

    static String nonNillableParameterIsNull(ResolvedFeatureId featureId, String paramName) {
        return "Non-nillable parameter " + paramName + " of " + featureId + " has not been initialized";
    }

    static String featureNotInScope(ResolvedFeatureId id, String groupName, ArtifactCoords.Gav fpGav) {
        final StringBuilder buf = new StringBuilder();
        buf.append(id).append(" cannot be included into group ").append(groupName);
        if(fpGav != null) {
            buf.append(" from ").append(fpGav);
        }
        buf.append(" as it's not in the scope of the group");
        return buf.toString();
    }

    static String unknownFeatureParameter(ResolvedSpecId specId, String paramName) {
        return new StringBuilder().append("Feature spec ").append(specId).append(" does not contain parameter '").append(paramName).toString();
    }

    static String featureIdParameterCantBeUnset(ResolvedFeatureId id, String paramName) {
        return new StringBuilder().append("Parameter ").append(paramName).append(" of ").append(id).append(" cannot be unset").toString();
    }

    static String featureIdParameterCantBeReset(ResolvedFeatureId id, String paramName) {
        return new StringBuilder().append("Parameter ").append(paramName).append(" of ").append(id).append(" cannot be reset").toString();
    }

    static String unknownFeatureParameter(String featureSpec, String paramName) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Feature spec ").append(featureSpec).append(" does not define parameter ").append(paramName);
        return buf.toString();
    }

    static String failedToProcess(ArtifactCoords.Gav fpGav, FeatureConfig feature) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to process feature-pack ").append(fpGav).append(" feature ").append(feature);
        return buf.toString();
    }

    static String failedToProcess(ArtifactCoords.Gav fpGav, String groupName) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to process feature-pack ").append(fpGav).append(" group ").append(groupName);
        return buf.toString();
    }

    static void appendConfig(final StringBuilder buf, String model, String name) {
        if (model != null) {
            buf.append(" model ").append(model);
        }
        if (name != null) {
            buf.append(" named ").append(name);
        }
    }

    static void appendFeature(StringBuilder buf, ResolvedFeature feature) {
        if (feature.hasId()) {
            buf.append(feature.getId());
        } else {
            buf.append(feature.getSpecId()).append(" configuration");
        }
    }
}
