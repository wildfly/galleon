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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.runtime.ResolvedFeature;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseFeaturePackInstaller;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
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

    static String deleteFile(Path p) {
        return "Failed to delete " + p.toAbsolutePath();
    }

    static String hashCalculation(Path path) {
        return "Hash calculation failed for " + path;
    }

    // FEATURE PACK INSTALL MESSAGES

    static String homeDirNotUsable(Path p) {
        return p + " has to be empty or contain a provisioned installation to be used by the tool";
    }

    static String fpVersionCheckFailed(Collection<Set<FeaturePackLocation.FPID>> versionConflicts) throws ProvisioningException {
        final StringWriter strWriter = new StringWriter();
        try(BufferedWriter writer = new BufferedWriter(strWriter)) {
            writer.write("Feature-pack versions check failed with the following errors:");
            writer.newLine();
            if(!versionConflicts.isEmpty()) {
                for (Collection<FeaturePackLocation.FPID> entry : versionConflicts) {
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

    static String failedToResolveReleaseVersions(Collection<FeaturePackLocation.ProducerSpec> channels) {
        final StringBuilder buf = new StringBuilder("Missing build number");
        if(channels.size() > 1) {
            buf.append('s');
        }
        buf.append(" for ");
        StringUtils.append(buf, channels);
        return buf.toString();
    }

    static String packageContentCopyFailed(String packageName) {
        return "Failed to copy package " + packageName + " content";
    }

    static String packageNotFound(FPID fpid, String packageName) {
        return "Failed to resolve package " + packageName + " in " + fpid;
    }

    static String unknownPackage(FeaturePackLocation.FPID fpid, String pkgName) {
        return "Package " + pkgName + " is not found in " + fpid;
    }

    static String unknownFeaturePack(FeaturePackLocation.FPID fpid) {
        return "Feature-pack " + fpid + " not found in the configuration";
    }

    static String unsatisfiedFeaturePackDep(ProducerSpec producer) {
        return "Feature-pack " + producer + " is a required dependency";
    }

    static String featurePackVersionConflict(FeaturePackLocation.FPID fpid1, FeaturePackLocation.FPID fpid2) {
        final Set<FeaturePackLocation.FPID> fpids = new LinkedHashSet<>(2);
        fpids.add(fpid1);
        fpids.add(fpid2);
        return featurePackVersionConflict(fpids);
    }

    static String featurePackVersionConflict(Collection<FeaturePackLocation.FPID> fpids) {
        final Iterator<FeaturePackLocation.FPID> i = fpids.iterator();
        FeaturePackLocation.FPID fpid = i.next();
        final StringBuilder buf = new StringBuilder("Please pick the desired build number for ")
                .append(fpid.getProducer())
                .append(" explicitly in the provisioning config. Current configuration references the following versions ")
                .append(fpid.getBuild());
        while(i.hasNext()) {
            fpid = i.next();
            buf.append(", ").append(fpid.getBuild());
        }
        return buf.toString();
    }

    static String unsatisfiedPackageDependencies(FeaturePackLocation.FPID fpid, String packageName, Collection<String> unsatisfiedDeps) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Feature-pack ").append(fpid).append(" package ").append(packageName).append(" has unsatisfied dependencies on packages: ");
        StringUtils.append(buf, unsatisfiedDeps);
        return buf.toString();
    }

    static String unsatisfiedPackageDependency(FPID fpid, String targetPackage) {
        return "Unsatisfied dependency on feature-pack " + fpid + " package " + targetPackage;
    }

    static String resolvePackage(FeaturePackLocation.FPID fpid, String packageName) {
        return "Failed to resolve feature-pack " + fpid + " package " + packageName;
    }

    static String packageExcludeInclude(String packageName) {
        return "Attempt to explicitly include and exclude package " + packageName;
    }

    static String duplicateDependencyName(String name) {
        return "Dependency with name " + name + " already exists";
    }

    static String unknownFeaturePackDependencyName(String depName) {
        return depName + " was not found among the feature-pack dependencies";
    }

    static String featurePackAlreadyConfigured(ProducerSpec producer) {
        return "Feature-pack " + producer + " already present in the configuration";
    }

    static String unknownFeaturePackDependencyName(FeaturePackLocation.FPID fpid, String pkgName, String depName) {
        return fpid + " package " + pkgName + " references unknown feature-pack dependency " + depName;
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
        buf.append("Failed to resolve configuration ");
        appendConfig(buf, model, name);
        return buf.toString();
    }

    static String failedToResolveConfigLayer(String model, String name) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to resolve configuration layer ");
        appendConfig(buf, model, name);
        return buf.toString();
    }

    static String failedToBuildConfigSpec(String model, String name) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to build configuration ");
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

    static String featureNotInScope(ResolvedFeatureId id, String groupName, FPID fpid) {
        final StringBuilder buf = new StringBuilder();
        buf.append(id).append(" cannot be included into group ").append(groupName);
        if(fpid != null) {
            buf.append(" from ").append(fpid);
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

    static String failedToProcess(FPID fpid, FeatureConfig feature) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to process feature-pack ").append(fpid).append(" feature ").append(feature);
        return buf.toString();
    }

    static String failedToProcess(FPID fpid, String groupName) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to process feature-pack ").append(fpid).append(" group ").append(groupName);
        return buf.toString();
    }

    static String frequencyNotSupported(final Collection<String> frequencies, FeaturePackLocation fpl) {
        final StringBuilder buf = new StringBuilder();
        buf.append("The frequency specified in ").append(fpl).append(" is not supported, the producer ");
        if (frequencies.isEmpty()) {
            buf.append(" does not suport frequencies");
        } else {
            buf.append("supported frequencies are ");
            final String[] arr = frequencies.toArray(new String[frequencies.size()]);
            Arrays.sort(arr);
            StringUtils.append(buf, Arrays.asList(arr));
        }
        return buf.toString();
    }

    static String transitiveDependencyNotFound(ProducerSpec... producer) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to locate ");
        StringUtils.append(buf, Arrays.asList(producer));
        buf.append(" among transitive dependencies");
        return buf.toString();
    }

    static String patchAlreadyApplied(FPID patchId) {
        return "Patch " + patchId + " has already been applied";
    }

    static String patchNotApplicable(FPID patchId, FPID targetFpid) {
        return "Patch " + patchId + " applies to " + targetFpid + " which is not a part of the installation";
    }

    static String featurePackInstallerNotFound(String universeFactory, Collection<String> installers) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Failed to locate an implementation of ")
        .append(UniverseFeaturePackInstaller.class.getName())
        .append(" for universe factory ")
        .append(universeFactory)
        .append(" on the classpath.");
        if(!installers.isEmpty()) {
            buf.append(" Available universe factory installers include ");
            StringUtils.append(buf, installers);
        }
        return buf.toString();
    }

    static String noVersionAvailable(FeaturePackLocation fpl) {
        return "No version is available for " + fpl;
    }

    static String historyIsEmpty() {
        return "Provisioning history is empty";
    }

    static String configLayerCanEitherBeIncludedOrExcluded(String configModel, String configName, String layerName) {
        return "Configuration layer " + layerName + " appears to be included and excluded in the same configuration " + (configModel == null ? configName : configModel + ':' + configName);
    }

    static String unsatisfiedLayerDependency(String srcLayer, String targetLayer) {
        return "Required dependency of configuration layer " + srcLayer + " on layer " + targetLayer + " was excluded";
    }

    static String fsEntryInit(Path p) {
        return "Failed to process child entries for " + p;
    }

    static String pluginOptionRequired(String name) {
        return "Provisioning option " + name + " is required for this configuration";
    }

    static String pluginOptionIllegalValue(String name, String setValue, Collection<String> values) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Provisioning option ").append(name).append(" value ").append(setValue).append(" is not in the legal value set: ");
        StringUtils.append(buf, values);
        return buf.toString();
    }

    static String pluginOptionsNotRecognized(Collection<String> names) {
        final StringBuilder buf = new StringBuilder("The following plugin options are not recognized: ");
        if(names.size() > 1) {
            final List<String> list = new ArrayList<>(names);
            Collections.sort(list);
            names = list;
        }
        StringUtils.append(buf, names);
        return buf.toString();
    }

    static String requiredPassiveDependency(String name) {
        return "Required dependency on " + name + " cannot be passive";
    }

    static String unexpectedPackageDependencyType(String name, int type) {
        return "Unexpected dependency type " + type + " on package " + name;
    }

    static String hashesNotPersisted() {
        return "Failed to persist hashes";
    }

    static String fileClose(Path p) {
        return "Failed to close file " + p;
    }

    static String classloaderClose() {
        return "Failed to close classloader";
    }

    static String topConfigsCantDefinePackageDeps(ConfigId configId) {
        final StringBuilder buf = new StringBuilder();
        buf.append("Config models defined in provisioning configuration are not allowed to define package dependencies: ");
        buf.append(configId);
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
