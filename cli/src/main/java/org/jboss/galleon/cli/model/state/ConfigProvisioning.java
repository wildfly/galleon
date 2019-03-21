/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.model.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigItem;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class ConfigProvisioning {

    private static class DefineConfigurationAction implements State.Action {

        private final ConfigId id;

        DefineConfigurationAction(ConfigId id) {
            this.id = id;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (ConfigModel m : builder.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    throw new ProvisioningException(CliErrors.configurationAlreadyExists(id));
                }
            }
            builder.addConfig(ConfigModel.builder(id.getModel(), id.getName()).build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeConfig(id);
        }
    }

    private static class IncludeLayersConfigurationAction implements State.Action {

        private final ConfigId id;
        private ConfigModel.Builder targetConfig;
        private final String[] layers;
        private boolean newConfig;
        private final State state;

        IncludeLayersConfigurationAction(ConfigId id, String[] layers, State state) {
            this.id = id;
            this.layers = layers;
            this.state = state;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (ConfigModel m : builder.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    targetConfig = ConfigModel.builder(m);
                    break;
                }
            }
            if (targetConfig == null) {
                // Must create a new one;
                targetConfig = ConfigModel.builder(id.getModel(), id.getName());
                newConfig = true;
            }
            //Check that layers are not already included.
            List<ConfigInfo> configs = state.getContainer().getFinalConfigs().get(id.getModel());
            ConfigInfo existingConfig = null;
            if (configs != null) {
                for (ConfigInfo ci : configs) {
                    if (ci.getName().equals(id.getName())) {
                        existingConfig = ci;
                    }
                }
            }
            if (existingConfig != null) {
                for (String l : layers) {
                    if (existingConfig.getlayers().contains(new ConfigId(id.getModel(), l))) {
                        throw new ProvisioningException(CliErrors.layerAlreadyExists(l, existingConfig.getlayers()));
                    }
                }
            }
            for (String layer : layers) {
                targetConfig.includeLayer(layer);
            }
            if (!newConfig) {
                builder.removeConfig(id);
            }
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeConfig(id);
            if (!newConfig) {
                for (String layer : layers) {
                    targetConfig.removeIncludedLayer(layer);
                }
                builder.addConfig(targetConfig.build());
            }
        }
    }

    private static class ExcludeLayersConfigurationAction implements State.Action {

        private final ConfigId id;
        private ConfigModel.Builder targetConfig;
        private final String[] layers;
        private boolean newConfig;
        private final State state;

        ExcludeLayersConfigurationAction(ConfigId id, String[] layers, State state) {
            this.id = id;
            this.layers = layers;
            this.state = state;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (ConfigModel m : builder.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    targetConfig = ConfigModel.builder(m);
                    break;
                }
            }
            if (targetConfig == null) {
                // Must create a new one;
                targetConfig = ConfigModel.builder(id.getModel(), id.getName());
                newConfig = true;
            }
            //Check that layers are included.
            List<ConfigInfo> configs = state.getContainer().getFinalConfigs().get(id.getModel());
            ConfigInfo existingConfig = null;
            if (configs != null) {
                for (ConfigInfo ci : configs) {
                    if (ci.getName().equals(id.getName())) {
                        existingConfig = ci;
                    }
                }
            }
            if (existingConfig != null) {
                for (String l : layers) {
                    if (!existingConfig.getlayers().contains(new ConfigId(id.getModel(), l))) {
                        throw new ProvisioningException(CliErrors.layerNotIncluded(l, existingConfig.getlayers()));
                    }
                }
            }
            for (String layer : layers) {
                targetConfig.excludeLayer(layer);
            }
            if (!newConfig) {
                builder.removeConfig(id);
            }
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeConfig(id);
            if (!newConfig) {
                for (String layer : layers) {
                    targetConfig.removeExcludedLayer(layer);
                }
                builder.addConfig(targetConfig.build());
            }
        }
    }

    private static class RemoveIncludedLayersConfigurationAction implements State.Action {

        private final ConfigId id;
        private ConfigModel.Builder targetConfig;
        private final String[] layers;

        RemoveIncludedLayersConfigurationAction(ConfigId id, String[] layers) {
            this.id = id;
            this.layers = layers;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            ConfigModel originalConfig = null;
            for (ConfigModel m : builder.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    targetConfig = ConfigModel.builder(m);
                    originalConfig = m;
                    break;
                }
            }
            if (targetConfig == null) {
                throw new ProvisioningException(CliErrors.configurationNotFound(id));
            }

            for (String layer : layers) {
                if (!originalConfig.getIncludedLayers().contains(layer)) {
                    throw new ProvisioningException(CliErrors.layerNotIncluded(layer, originalConfig.getIncludedLayers()));
                }
                targetConfig.removeIncludedLayer(layer);
            }
            builder.removeConfig(id);
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeConfig(id);
            for (String layer : layers) {
                targetConfig.includeLayer(layer);
            }
            builder.addConfig(targetConfig.build());
        }
    }

    private static class RemoveExcludedLayersConfigurationAction implements State.Action {

        private final ConfigId id;
        private ConfigModel.Builder targetConfig;
        private final String[] layers;

        RemoveExcludedLayersConfigurationAction(ConfigId id, String[] layers) {
            this.id = id;
            this.layers = layers;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            ConfigModel originalConfig = null;
            for (ConfigModel m : builder.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    targetConfig = ConfigModel.builder(m);
                    originalConfig = m;
                    break;
                }
            }
            if (targetConfig == null) {
                throw new ProvisioningException(CliErrors.configurationNotFound(id));
            }
            for (String layer : layers) {
                if (!originalConfig.getExcludedLayers().contains(layer)) {
                    throw new ProvisioningException(CliErrors.layerNotExcluded(layer, originalConfig.getExcludedLayers()));
                }
                targetConfig.removeExcludedLayer(layer);
            }
            builder.removeConfig(id);
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeConfig(id);
            for (String layer : layers) {
                targetConfig.excludeLayer(layer);
            }
            builder.addConfig(targetConfig.build());
        }
    }

    private static class ResetConfigurationAction implements State.Action {

        private final ConfigId id;
        private ConfigModel config;
        private List<ConfigModel> originalList;

        ResetConfigurationAction(ConfigId id) {
            this.id = id;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            for (ConfigModel m : current.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    config = m;
                    break;
                }
            }
            if (config == null) {
                throw new ProvisioningException(CliErrors.configurationNotFound(id));
            }
            originalList = new ArrayList<>(builder.getDefinedConfigs());
            builder.removeConfig(id);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeAllConfigs();
            for (ConfigModel config : originalList) {
                builder.addConfig(config);
            }
        }
    }

    private static class AddFeatureAction implements State.Action {

        private final ConfigId id;
        private final FeatureSpecInfo spec;
        private final Map<String, String> options;
        private ConfigModel.Builder targetConfig;
        private FeatureId featId;
        private boolean newConfig;
        private boolean isExcluded;
        private FeatureConfig existingFeature;

        AddFeatureAction(ConfigId id, FeatureSpecInfo spec, Map<String, String> options) {
            this.id = id;
            this.spec = spec;
            this.options = options;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            Map<String, String> ids = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            for (String opt : options.keySet()) {
                FeatureParameterSpec p = spec.getSpec().getParams().get(opt);
                String value = options.get(opt);
                if (p.isFeatureId()) {
                    if (!Constants.GLN_UNDEFINED.equals(value)) {
                        ids.put(opt, value);
                    }
                } else if (!value.equals(p.getDefaultValue())) {
                    params.put(opt, value);
                }
            }
            featId = new FeatureId(spec.getSpec().getName(), ids);
            FeatureConfig fc = FeatureConfig.newConfig(featId);

            for (ConfigModel cm : current.getDefinedConfigs()) {
                if (cm.getName().equals(id.getName()) && cm.getModel().equals(id.getModel())) {
                    targetConfig = ConfigModel.builder(cm);
                    existingFeature = getExistingFeature(cm, featId);
                    if (existingFeature != null) {
                        targetConfig.removeFeature(featId);
                    }
                    isExcluded = cm.getExcludedFeatures().containsKey(featId);
                    break;
                }
            }

            if (targetConfig == null) {
                // Must create a new one;
                targetConfig = ConfigModel.builder(id.getModel(), id.getName());
                newConfig = true;
            }
            for (Map.Entry<String, String> p : params.entrySet()) {
                fc.setParam(p.getKey(), p.getValue());
            }
            if (isExcluded) {
                targetConfig.removeExcludedFeature(featId);
            } else {
                targetConfig.addFeature(fc);
            }
            if (!newConfig) {
                builder.removeConfig(id);
            }
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (isExcluded) {
                targetConfig.excludeFeature(featId);
            } else {
                targetConfig.removeFeature(featId);
                if (existingFeature != null) {
                    targetConfig.addFeature(existingFeature);
                }
            }
            builder.removeConfig(id);
            if (!newConfig) {
                builder.addConfig(targetConfig.build());
            }
        }
    }

    private static class RemoveFeatureAction implements State.Action {

        private final ConfigId id;
        private final FeatureInfo feature;
        private ConfigModel.Builder targetConfig;
        private boolean newConfig;
        private boolean exclude;

        RemoveFeatureAction(ConfigId id, FeatureInfo feature) {
            this.id = id;
            this.feature = feature;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (feature.getFeatureId() == null) {
                throw new ProvisioningException("Feature " + feature.getType() + " can't be removed, no feature-id");
            }
            // first retrieve the configuration to remove the feature.
            for (ConfigModel cm : current.getDefinedConfigs()) {
                if (cm.getName().equals(id.getName()) && cm.getModel().equals(id.getModel())) {
                    targetConfig = ConfigModel.builder(cm);
                    exclude = getExistingFeature(cm, feature.getFeatureId()) == null;
                    break;
                }
            }
            if (targetConfig == null) {
                // It has been included or is inherited.
                if (!current.isInheritConfigs()) {
                    if (!current.getIncludedConfigs().contains(id)) {
                        throw new ProvisioningException(CliErrors.configurationNotFound(id));
                    }
                }
                // So we need to create a config Item to exlude the feature.
                targetConfig = ConfigModel.builder(id.getModel(), id.getName());
                newConfig = true;
                targetConfig.excludeFeature(feature.getFeatureId());
            } else {
                builder.removeConfig(id);
                if (exclude) {
                    targetConfig.excludeFeature(feature.getFeatureId());
                } else {
                    targetConfig.removeFeature(feature.getFeatureId());
                }
            }
            builder.addConfig(targetConfig.build());
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (newConfig) {
                builder.removeConfig(id);
            } else {
                if (exclude) {
                    targetConfig.removeExcludedFeature(feature.getFeatureId());
                } else {
                    targetConfig.addFeature(feature.getFeatureConfig());
                }
                builder.removeConfig(id);
                builder.addConfig(targetConfig.build());
            }
        }
    }

    private static FeatureConfig getExistingFeature(ConfigModel cm, FeatureId featId) {
        for (ConfigItem ci : cm.getItems()) {
            if (ci instanceof FeatureConfig) {
                FeatureConfig fi = (FeatureConfig) ci;
                if (fi.getSpecId().equals(featId.getSpec())) {
                    boolean eq = true;
                    for (String name : featId.getParamNames()) {
                        String p = fi.getParam(name);
                        if (p == null || !p.equals(featId.getParam(name))) {
                            eq = false;
                            break;
                        }
                    }
                    if (eq) {
                        return fi;
                    }
                }
            }
        }
        return null;
    }

    State.Action resetConfiguration(ConfigId id) {
        return new ResetConfigurationAction(id);
    }

    State.Action includeLayersConfiguration(ConfigId id, String[] layers, State state) {
        return new IncludeLayersConfigurationAction(id, layers, state);
    }

    State.Action excludeLayersConfiguration(ConfigId id, String[] layers, State state) {
        return new ExcludeLayersConfigurationAction(id, layers, state);
    }

    State.Action removeIncludedLayersConfiguration(ConfigId id, String[] layers) {
        return new RemoveIncludedLayersConfigurationAction(id, layers);
    }

    State.Action removeExcludedLayersConfiguration(ConfigId id, String[] layers) {
        return new RemoveExcludedLayersConfigurationAction(id, layers);
    }

    State.Action newConfiguration(ConfigId id) {
        return new DefineConfigurationAction(id);
    }

    State.Action addFeature(ConfigId id, FeatureSpecInfo spec, Map<String, String> options) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new AddFeatureAction(id, spec, options);
    }

    State.Action removeFeature(ConfigId id, FeatureInfo fi) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveFeatureAction(id, fi);
    }
}
