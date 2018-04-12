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
package org.jboss.galleon.cli.cmd.state.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.MavenArtifactRepositoryManager;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class ConfigurationUtil extends AbstractFPProvisionedCommand {

    public static Map<FeaturePackConfig, ConfigId> getConfigurations(PmSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getGav(), configuration);
                if (info != null) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getGav(), configuration);
            if (info != null) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    public static Map<FeaturePackConfig, ConfigId> getIncludedConfigurations(PmSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getGav(), configuration);
                if (info != null && c.getIncludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getGav(), configuration);
            if (info != null && config.getIncludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    public static Map<FeaturePackConfig, ConfigId> getExcludedConfigurations(PmSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getGav(), configuration);
                if (info != null && c.getExcludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getGav(), configuration);
            if (info != null && config.getExcludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    private static ConfigInfo getConfig(PmSession session, ArtifactCoords.Gav gav, String configuration) throws ProvisioningException, IOException, PathParserException, PathConsumerException {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + configuration + PathParser.PATH_SEPARATOR;
        FeatureContainer full = FeatureContainers.fromFeaturePackGav(session, ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance()).build(), gav, null);
        ConfigInfo ci = null;
        try {
            FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(full, false);
            PathParser.parse(path, consumer);
            ci = consumer.getConfig();
        } catch (PathParserException | PathConsumerException ex) {
            // XXX OK, return null
        }
        return ci;
    }

    public static String getCurrentPath(PmSession session) {
        return FeatureContainerPathConsumer.FINAL_CONFIGS_PATH;
    }

    public static void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
        if (consumer.getConfig() != null) {
            candidates.clear();
        }
        if (consumer.getConfigModel() != null) {
            for (int i = 0; i < candidates.size(); i++) {
                String c = candidates.get(i);
                if (c.endsWith("" + PathParser.PATH_SEPARATOR)) {
                    candidates.set(i, c.substring(0, c.length() - 1));
                }
            }
        }
    }

}
