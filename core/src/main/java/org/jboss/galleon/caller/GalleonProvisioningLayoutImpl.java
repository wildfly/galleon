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
package org.jboss.galleon.caller;

import java.util.ArrayList;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.GalleonFeaturePackLayout;
import org.jboss.galleon.api.GalleonProvisioningLayout;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise
 */
class GalleonProvisioningLayoutImpl implements GalleonProvisioningLayout {

    private final ProvisioningLayout<FeaturePackLayout> layout;

    GalleonProvisioningLayoutImpl(ProvisioningLayout<FeaturePackLayout> layout) {
        this.layout = layout;
    }

    @Override
    public void close() {
        layout.close();
    }

    ProvisioningLayout<FeaturePackLayout> getLayout() {
        return layout;
    }

    @Override
    public boolean hasPlugins() {
        return layout.hasPlugins();
    }

    @Override
    public List<GalleonFeaturePackLayout> getOrderedFeaturePacks() {
        List<GalleonFeaturePackLayout> lst = new ArrayList<>();
        for (FeaturePackLayout l : layout.getOrderedFeaturePacks()) {
            lst.add(l);
        }
        return lst;
    }

    @Override
    public List<GalleonFeaturePackLayout> getPatches(FeaturePackLocation.FPID fpid) {
        List<GalleonFeaturePackLayout> lst = new ArrayList<>();
        for (FeaturePackLayout l : layout.getPatches(fpid)) {
            lst.add(l);
        }
        return lst;
    }

    @Override
    public void uninstall(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        layout.uninstall(fpid);
    }

    @Override
    public GalleonFeaturePackLayout getFeaturePack(FeaturePackLocation.ProducerSpec spec) throws ProvisioningException {
        return layout.getFeaturePack(spec);
    }

}
