/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureContainers;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class ConfigurationUtil extends AbstractFPProvisionedCommand {

    public static Map<FeaturePackConfig, ConfigId> getConfigurations(ProvisioningSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getLocation().getFPID(), configuration);
                if (info != null) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getLocation().getFPID(), configuration);
            if (info != null) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    public static Map<FeaturePackConfig, ConfigId> getIncludedConfigurations(ProvisioningSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getLocation().getFPID(), configuration);
                if (info != null && c.getIncludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getLocation().getFPID(), configuration);
            if (info != null && config.getIncludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    public static Map<FeaturePackConfig, ConfigId> getExcludedConfigurations(ProvisioningSession session, FeaturePackConfig config, String configuration) throws PathParserException, PathConsumerException, ProvisioningException, Exception {
        Map<FeaturePackConfig, ConfigId> configs = new HashMap<>();
        if (config == null) {
            for (FeaturePackConfig c : session.getState().getConfig().getFeaturePackDeps()) {
                ConfigInfo info = getConfig(session, c.getLocation().getFPID(), configuration);
                if (info != null && c.getExcludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                    configs.put(c, info.getId());
                }
            }
        } else {
            ConfigInfo info = getConfig(session, config.getLocation().getFPID(), configuration);
            if (info != null && config.getExcludedConfigs().contains(new ConfigId(info.getModel(), info.getName()))) {
                configs.put(config, info.getId());
            }
        }
        if (configs.isEmpty()) {
            throw new ProvisioningException("Not a valid config " + configuration);
        }
        return configs;
    }

    private static ConfigInfo getConfig(ProvisioningSession session, FPID fpid, String configuration) throws ProvisioningException, IOException, PathParserException, PathConsumerException {
        String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH + configuration + PathParser.PATH_SEPARATOR;
        FeatureContainer full = FeatureContainers.fromFeaturePackId(session, fpid, null);
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
