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
package org.jboss.galleon.cli.model.state;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
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

    private class ResetConfigurationAction implements State.Action {

        private final ConfigId id;
        private int index;
        ResetConfigurationAction(ConfigId id) {
            this.id = id;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            boolean found = false;
            for (ConfigModel m : current.getDefinedConfigs()) {
                if (m.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ProvisioningException("Config " + id + " doesn't exist");
            }
            index = builder.getDefinedConfigIndex(id);
            builder.removeConfig(id);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.addConfig(index, ConfigModel.builder().setModel(id.getModel()).setName(id.getName()).build());
        }

    }

    private class AddFeatureAction implements State.Action {

        private final ConfigId id;
        private final FeatureSpecInfo spec;
        private final Map<String, String> options;
        private ConfigModel.Builder targetConfig;
        private FeatureId featId;
        private boolean newConfig;
        private boolean isExcluded;
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
                    if (!Constants.PM_UNDEFINED.equals(value)) {
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
                    targetConfig = cm.getBuilder();
                    if (featureExists(cm, featId)) {
                        throw new ProvisioningException("Feature " + featId + "already exists in config " + id);
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
            }
            builder.removeConfig(id);
            if (!newConfig) {
                builder.addConfig(targetConfig.build());
            }
        }
    }

    private class RemoveFeatureAction implements State.Action {

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
            // first retrieve the configuration to remove the feature.
            for (ConfigModel cm : current.getDefinedConfigs()) {
                if (cm.getName().equals(id.getName()) && cm.getModel().equals(id.getModel())) {
                    targetConfig = cm.getBuilder();
                    exclude = !featureExists(cm, feature.getFeatureId());
                    break;
                }
            }
            if (targetConfig == null) {
                // It has been included or is inherited.
                if (!current.isInheritConfigs()) {
                    if (!current.getIncludedConfigs().contains(id)) {
                        throw new ProvisioningException("Unknown config " + targetConfig);
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

    private static boolean featureExists(ConfigModel cm, FeatureId featId) {
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
                        return true;
                    }
                }
            }
        }
        return false;
    }

    State.Action resetConfiguration(ConfigId id) {
        return new ResetConfigurationAction(id);
    }

    State.Action addFeature(ConfigId id, FeatureSpecInfo spec, Map<String, String> options) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new AddFeatureAction(id, spec, options);
    }

    State.Action removeFeature(ConfigId id, FeatureInfo fi) throws ProvisioningDescriptionException, ProvisioningException, IOException {
        return new RemoveFeatureAction(id, fi);
    }
}
