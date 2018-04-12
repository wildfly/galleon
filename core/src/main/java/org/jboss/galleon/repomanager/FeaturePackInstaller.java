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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.ProvisioningDescriptionException;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FeaturePackInstaller {

    private List<FeaturePackBuilder> fps = Collections.emptyList();
    private final ArtifactRepositoryManager outer;

    FeaturePackInstaller(final ArtifactRepositoryManager outer) {
        this.outer = outer;
    }

    public FeaturePackBuilder newFeaturePack() {
        return newFeaturePack(null);
    }

    public FeaturePackBuilder newFeaturePack(ArtifactCoords.Gav gav) {
        final FeaturePackBuilder fp = FeaturePackBuilder.newInstance(this);
        if (gav != null) {
            fp.setGav(gav);
        }
        addFeaturePack(fp);
        return fp;
    }

    public FeaturePackInstaller addFeaturePack(FeaturePackBuilder fp) {
        switch (fps.size()) {
            case 0:
                fps = Collections.singletonList(fp);
                break;
            case 1:
                fps = new ArrayList<>(fps);
            default:
                fps.add(fp);
        }
        return this;
    }

    public void install() throws ProvisioningDescriptionException {
        for (FeaturePackBuilder fp : fps) {
            fp.build(outer);
        }
    }

}
