/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.config.GalleonConfigurationWithLayers;
import org.jboss.galleon.api.config.GalleonFeaturePackConfig;
import org.jboss.galleon.api.config.GalleonProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;
import org.jboss.galleon.xml.ConfigXmlParser;

/**
 * The configuration of the installation to be provisioned.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningConfig extends FeaturePackDepsConfig {

    public static class Builder extends FeaturePackDepsConfigBuilder<Builder> {

        private Map<String, String> options = Collections.emptyMap();

        private Builder() {
        }

        private Builder(ProvisioningConfig original) throws ProvisioningDescriptionException {
            if (original == null) {
                return;
            }
            if (original.hasOptions()) {
                addOptions(original.getOptions());
            }
            for (FeaturePackConfig fp : original.getFeaturePackDeps()) {
                addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
            }
            if (original.hasTransitiveDeps()) {
                for (FeaturePackConfig fp : original.getTransitiveDeps()) {
                    addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
                }
            }
            initUniverses(original);
            initConfigs(original);
        }

        public Builder addOption(String name, String value) {
            options = CollectionUtils.put(options, name, value);
            return this;
        }

        public Builder removeOption(String name) {
            options = CollectionUtils.remove(options, name);
            return this;
        }

        public Builder clearOptions() {
            options = Collections.emptyMap();
            return this;
        }

        public Builder addOptions(Map<String, String> options) {
            this.options = CollectionUtils.putAll(this.options, options);
            return this;
        }

        public ProvisioningConfig build() throws ProvisioningDescriptionException {
            return new ProvisioningConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ProvisioningConfig toConfig(GalleonProvisioningConfig gConfig) throws ProvisioningException, ProvisioningDescriptionException {
        return toConfig(gConfig,Collections.emptyList());
    }
    public static ProvisioningConfig toConfig(GalleonProvisioningConfig gConfig, List<Path> customConfigs) throws ProvisioningException, ProvisioningDescriptionException {
        Builder builder = ProvisioningConfig.builder();
        builder.addOptions(gConfig.getOptions());
        for (ConfigId c : gConfig.getExcludedConfigs()) {
            builder.excludeDefaultConfig(c);
        }
        for (GalleonFeaturePackConfig dep : gConfig.getFeaturePackDeps()) {
            builder.addFeaturePackDep(toFeaturePackConfig(dep));
        }
        for (GalleonFeaturePackConfig tc : gConfig.getTransitiveDeps()) {
            builder.addFeaturePackDep(toFeaturePackConfig(tc));
        }
        for (Entry<String, UniverseSpec> entry : gConfig.getUniverseNamedSpecs().entrySet()) {
            builder.addUniverse(entry.getKey(), entry.getValue());
        }
        if (gConfig.getInheritConfigs() != null) {
            builder.setInheritConfigs(gConfig.getInheritConfigs());
        }
        builder.setDefaultUniverse(gConfig.getDefaultUniverse());
        builder.setInheritModelOnlyConfigs(gConfig.isInheritModelOnlyConfigs());
        for (ConfigId c : gConfig.getExcludedConfigs()) {
            builder.excludeDefaultConfig(c);
        }
        for (Entry<String, Boolean> entry : gConfig.getFullModelsExcluded().entrySet()) {
            builder.excludeConfigModel(entry.getKey(), entry.getValue());
        }
        for (String model : gConfig.getFullModelsIncluded()) {
            builder.includeConfigModel(model);
        }
        for (ConfigId id : gConfig.getIncludedConfigs()) {
            builder.includeDefaultConfig(id);
        }
        for (GalleonConfigurationWithLayers configuration : gConfig.getDefinedConfigs()) {
            if (configuration instanceof ConfigModel) {
                builder.addConfig((ConfigModel) configuration);
            } else {
                ConfigModel.Builder cBuilder = ConfigModel.builder(configuration.getModel(), configuration.getName());
                for (String l : configuration.getIncludedLayers()) {
                    cBuilder.includeLayer(l);
                }
                for (String l : configuration.getExcludedLayers()) {
                    cBuilder.excludeLayer(l);
                }
                for (Entry<String, String> p : configuration.getProperties().entrySet()) {
                    cBuilder.setProperty(p.getKey(), p.getValue());
                }
                builder.addConfig(cBuilder.build());
            }
        }
        for (Path custom : customConfigs) {
            builder.addConfig(parseConfigurationFile(custom));
        }
        return builder.build();
    }

    public static FeaturePackConfig toFeaturePackConfig(GalleonFeaturePackConfig dep) throws ProvisioningDescriptionException {
        FeaturePackConfig.Builder fpBuilder = dep.isTransitive() ? FeaturePackConfig.transitiveBuilder(dep.getLocation()) : FeaturePackConfig.builder(dep.getLocation());
        for (ConfigId c : dep.getExcludedConfigs()) {
            fpBuilder.excludeDefaultConfig(c);
        }
        fpBuilder.excludeAllPackages(dep.getExcludedPackages());
        for (Entry<String, Boolean> entry : dep.getFullModelsExcluded().entrySet()) {
            fpBuilder.excludeConfigModel(entry.getKey(), entry.getValue());
        }
        for (String model : dep.getFullModelsIncluded()) {
            fpBuilder.includeConfigModel(model);
        }
        for (ConfigId id : dep.getIncludedConfigs()) {
            fpBuilder.includeDefaultConfig(id);
        }
        fpBuilder.includeAllPackages(dep.getIncludedPackages());
        if (dep.getInheritConfigs() != null) {
            fpBuilder.setInheritConfigs(dep.getInheritConfigs());
        }
        if (dep.getInheritPackages() != null) {
            fpBuilder.setInheritPackages(dep.getInheritPackages());
        }
        for (FPID patch : dep.getPatches()) {
            fpBuilder.addPatch(patch);
        }
        return fpBuilder.build();
    }

    private static GalleonFeaturePackConfig toFeaturePackConfig(FeaturePackConfig dep) throws ProvisioningDescriptionException {
        GalleonFeaturePackConfig.Builder fpBuilder = dep.isTransitive() ? GalleonFeaturePackConfig.transitiveBuilder(dep.getLocation()) : GalleonFeaturePackConfig.builder(dep.getLocation());
        for (ConfigId c : dep.getExcludedConfigs()) {
            fpBuilder.excludeDefaultConfig(c);
        }
        fpBuilder.excludeAllPackages(dep.getExcludedPackages());
        for (Entry<String, Boolean> entry : dep.getFullModelsExcluded().entrySet()) {
            fpBuilder.excludeConfigModel(entry.getKey(), entry.getValue());
        }
        for (String model : dep.getFullModelsIncluded()) {
            fpBuilder.includeConfigModel(model);
        }
        for (ConfigId id : dep.getIncludedConfigs()) {
            fpBuilder.includeDefaultConfig(id);
        }
        fpBuilder.includeAllPackages(dep.getIncludedPackages());
        if (dep.getInheritConfigs() != null) {
            fpBuilder.setInheritConfigs(dep.getInheritConfigs());
        }
        if (dep.getInheritPackages() != null) {
            fpBuilder.setInheritPackages(dep.getInheritPackages());
        }
        for (FPID patch : dep.getPatches()) {
            fpBuilder.addPatch(patch);
        }
        return fpBuilder.build();
    }

    private static ConfigModel parseConfigurationFile(Path configuration) throws ProvisioningException {
        try (BufferedReader reader = Files.newBufferedReader(configuration)) {
            return ConfigXmlParser.getInstance().parse(reader);
        } catch (XMLStreamException | IOException ex) {
            throw new ProvisioningException("Couldn't load the customization configuration " + configuration, ex);
        }
    }

    public static GalleonProvisioningConfig toConfig(ProvisioningConfig gConfig) throws ProvisioningDescriptionException {
        GalleonProvisioningConfig.Builder builder = GalleonProvisioningConfig.builder();
        builder.addOptions(gConfig.getOptions());
        for (ConfigId c : gConfig.getExcludedConfigs()) {
            builder.excludeDefaultConfig(c);
        }
        for (FeaturePackConfig dep : gConfig.getFeaturePackDeps()) {
            builder.addFeaturePackDep(toFeaturePackConfig(dep));
        }
        for (FeaturePackConfig tc : gConfig.getTransitiveDeps()) {
            builder.addFeaturePackDep(toFeaturePackConfig(tc));
        }
        for (Entry<String, UniverseSpec> entry : gConfig.getUniverseNamedSpecs().entrySet()) {
            builder.addUniverse(entry.getKey(), entry.getValue());
        }
        if (gConfig.getInheritConfigs() != null) {
            builder.setInheritConfigs(gConfig.getInheritConfigs());
        }
        builder.setDefaultUniverse(gConfig.getDefaultUniverse());
        builder.setInheritModelOnlyConfigs(gConfig.isInheritModelOnlyConfigs());
        for (ConfigId c : gConfig.getExcludedConfigs()) {
            builder.excludeDefaultConfig(c);
        }
        for (Entry<String, Boolean> entry : gConfig.getFullModelsExcluded().entrySet()) {
            builder.excludeConfigModel(entry.getKey(), entry.getValue());
        }
        for (String model : gConfig.getFullModelsIncluded()) {
            builder.includeConfigModel(model);
        }
        for (ConfigId id : gConfig.getIncludedConfigs()) {
            builder.includeDefaultConfig(id);
        }
        for(ConfigModel m : gConfig.getDefinedConfigs()) {
            builder.addConfig(m);
        }
        return builder.build();
    }

    /**
     * Allows to build a provisioning configuration starting from the passed in
     * initial configuration.
     *
     * @param provisioningConfig initial state of the configuration to be built
     * @return this builder instance
     * @throws ProvisioningDescriptionException in case the config couldn't be
     * built
     */
    public static Builder builder(ProvisioningConfig provisioningConfig) throws ProvisioningDescriptionException {
        return new Builder(provisioningConfig);
    }

    private final Map<String, String> options;

    private ProvisioningConfig(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.options = CollectionUtils.unmodifiable(builder.options);
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    public String getOption(String name) {
        return options.get(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProvisioningConfig other = (ProvisioningConfig) obj;
        if (options == null) {
            if (other.options != null) {
                return false;
            }
        } else if (!options.equals(other.options)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append('[');
        append(buf);
        if (!options.isEmpty()) {
            buf.append("options=");
            StringUtils.append(buf, options.entrySet());
        }
        return buf.append(']').toString();
    }
}
