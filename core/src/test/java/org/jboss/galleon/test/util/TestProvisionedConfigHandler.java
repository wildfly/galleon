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
package org.jboss.galleon.test.util;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.plugin.ProvisionedConfigHandler;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.runtime.ResolvedFeatureSpec;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeature;
import org.junit.Assert;

/**
 * @author Alexey Loubyansky
 *
 */
public abstract class TestProvisionedConfigHandler implements ProvisionedConfigHandler {

    private static final String BATCH_START = "START BATCH";
    private static final String BATCH_END = "END BATCH";
    private static final String BRANCH_START = "START BRANCH";
    private static final String BRANCH_END = "END BRANCH";

    protected static String batchStartEvent() {
        return BATCH_START;
    }

    protected static String batchEndEvent() {
        return BATCH_END;
    }

    protected static String branchStartEvent() {
        return BRANCH_START;
    }

    protected static String branchEndEvent() {
        return BRANCH_END;
    }

    protected static String featurePackEvent(ArtifactCoords.Gav fpGav) {
        return "feature-pack " + fpGav;
    }

    protected static String specEvent(String spec) {
        return " spec " + spec;
    }

    protected static String featureEvent(ResolvedFeatureId id) {
        return "  " + id;
    }

    protected final boolean logEvents = loggingEnabled();
    private int i = 0;
    private final String[] events;
    private final boolean branchesOn = branchesEnabled();

    protected TestProvisionedConfigHandler() {
        try {
            events = initEvents();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize events", e);
        }
    }

    protected boolean loggingEnabled() {
        return false;
    }

    protected boolean branchesEnabled() {
        return false;
    }

    protected abstract String[] initEvents() throws Exception;

    @Override
    public void prepare(ProvisionedConfig config) {
        i = 0;
    }

    @Override
    public void startBranch() {
        if(branchesOn) {
            assertNextEvent(branchStartEvent());
        }
    }

    @Override
    public void endBranch() {
        if(branchesOn) {
            assertNextEvent(branchEndEvent());
        }
    }

    @Override
    public void startBatch() {
        assertNextEvent(batchStartEvent());
    }

    @Override
    public void endBatch() {
        assertNextEvent(batchEndEvent());
    }

    @Override
    public void nextFeaturePack(ArtifactCoords.Gav fpGav) {
        assertNextEvent(featurePackEvent(fpGav));
    }

    @Override
    public void nextSpec(ResolvedFeatureSpec spec) {
        assertNextEvent(specEvent(spec.getName()));
    }

    @Override
    public void nextFeature(ProvisionedFeature feature) {
        assertNextEvent(featureEvent(feature.getId()));
    }

    private void assertNextEvent(String actual) {
        if(logEvents) {
            System.out.println(actual);
        }
        Assert.assertEquals(events[i++], actual);
    }
}
