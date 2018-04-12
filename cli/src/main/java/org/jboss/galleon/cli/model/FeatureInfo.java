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
package org.jboss.galleon.cli.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Gav;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.state.ProvisionedFeature;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureInfo {

    private final ProvisionedFeature feature;
    private final List<String> path;
    private final Gav currentFP;
    private FeatureSpecInfo specInfo;
    private final FeatureId featureId;
    private final FeatureConfig featureConfig;
    private final String fullPath;
    private final ConfigInfo configInfo;

    public FeatureInfo(ConfigInfo configInfo, ProvisionedFeature feature, List<String> path, Gav currentFP) throws ProvisioningDescriptionException {
        this.feature = feature;
        this.path = path;
        this.currentFP = currentFP;
        this.configInfo = configInfo;
        FeatureId.Builder builder = FeatureId.builder(feature.getId().getSpecId().getName());
        for (Entry<String, Object> param : feature.getId().getParams().entrySet()) {
            builder.setParam(param.getKey(), param.getValue().toString());
        }
        featureId = builder.build();
        featureConfig = FeatureConfig.newConfig(featureId);
        StringBuilder b = new StringBuilder();
        b.append(FeatureContainerPathConsumer.FINAL_CONFIGS_PATH).append(configInfo.getModel()).
                append(PathParser.PATH_SEPARATOR).append(configInfo.getName()).append(PathParser.PATH_SEPARATOR);
        for (int i = 0; i < path.size(); i++) {
            b.append(path.get(i));
            if (i < path.size() - 1) {
                b.append(PathParser.PATH_SEPARATOR);
            }
        }
        fullPath = b.toString();
    }

    public String getPath() {
        return fullPath;
    }

    void attachSpecInfo(FeatureSpecInfo specInfo) {
        this.specInfo = specInfo;
        for (Map.Entry<String, Object> p : feature.getResolvedParams().entrySet()) {
            if (!specInfo.getSpec().getParams().get(p.getKey()).isFeatureId()) {
                featureConfig.setParam(p.getKey(), p.getValue().toString());
            }
        }
    }
    public FeatureId getFeatureId() {
        return featureId;
    }

    public FeatureConfig getFeatureConfig() {
        return featureConfig;
    }

    public String getDescription() {
        return "no description available";
    }
    public ResolvedSpecId getSpecId() {
        return feature.getSpecId();
    }

    public String getName() {
        // Return last path item.
        return path.get(path.size() - 1);
    }

    public String getType() {
        return specInfo.getType();
    }

    public Map<String, Object> getResolvedParams() throws ProvisioningException {
//        List<String> res = new ArrayList<>();
//        for (String param : feature.getParamNames()) {
//            Object val = feature.getConfigParam(param);
//            //if (!feature.getId().getParams().containsKey(param) && !PM_UNDEFINED.equals(val)) {
//                res.add(param + "=" + val);
//            //}
//        }
//        Collections.sort(res);
        try {
            return feature.getResolvedParams();
        } catch (Exception ex) {
            // fallback
            Map<String, Object> map = new HashMap<>();
            for (String param : feature.getParamNames()) {
                map.put(param, feature.getConfigParam(param));
            }
            return map;
        }
    }

    public List<String> getUndefinedParams() throws ProvisioningException {
        List<String> res = new ArrayList<>();
        for (String p : specInfo.getAllParameters()) {
            if (feature.getConfigParam(p) == null) {
                res.add(p);
            }
        }
        Collections.sort(res);
        return res;
    }
}
