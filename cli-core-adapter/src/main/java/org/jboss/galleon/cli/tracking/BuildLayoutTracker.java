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
package org.jboss.galleon.cli.tracking;

import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class BuildLayoutTracker extends CliProgressTracker<FPID> {

    private final ProvisioningSession session;

    public BuildLayoutTracker(ProvisioningSession session) {
        super(session, "Resolving feature-pack", "Feature-packs resolved.");
        this.session = session;
    }

    @Override
    protected String processingContent(ProgressTracker<FPID> tracker) {
        return session.getExposedLocation(null, tracker.getItem().getLocation()).toString();
    }

    @Override
    protected String completeContent(ProgressTracker<FPID> tracker) {
        return "";
    }

}
