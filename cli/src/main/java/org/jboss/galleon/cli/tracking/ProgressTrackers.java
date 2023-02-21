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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.progresstracking.DefaultProgressTracker;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class ProgressTrackers {

    private static final Map<String, CliProgressTracker<?>> trackers = new HashMap<>();
    private ProgressTrackers() {
    }

    public static void registerTrackers(PmSession session) {
        init(session);
        for (Entry<String, CliProgressTracker<?>> entry : trackers.entrySet()) {
            session.getLayoutFactory().setProgressCallback(entry.getKey(), entry.getValue());
        }
    }

    public static void unregisterTrackers(PmSession session) {
        for (Entry<String, CliProgressTracker<?>> entry : trackers.entrySet()) {
            session.getLayoutFactory().setProgressCallback(entry.getKey(), null);
        }
    }

    public static void commandStart(PmCommandInvocation session) {
        init(session.getPmSession());
        for (CliProgressTracker<?> tracker : trackers.values()) {
            tracker.commandStart(session);
        }
    }

    public static void commandEnd(PmCommandInvocation session) {
        init(session.getPmSession());
        for (CliProgressTracker<?> tracker : trackers.values()) {
            tracker.commandEnd(session);
        }
    }

    private static void init(PmSession session) {
        if (trackers.isEmpty()) {
            BuildLayoutTracker layout = new BuildLayoutTracker(session);
            trackers.put(ProvisioningLayoutFactory.TRACK_LAYOUT_BUILD, layout);

            PackagesTracker packages = new PackagesTracker(session);
            trackers.put(ProvisioningLayoutFactory.TRACK_PACKAGES, packages);

            trackers.put("JBMODULES", new JBossModulesTracker(session));
            trackers.put("JBEXTRACONFIGS", new ExtraConfigsTracker(session));
            ConfigsTracker configs = new ConfigsTracker(session);
            trackers.put(ProvisioningLayoutFactory.TRACK_CONFIGS, configs);

            UpdatesTracker updates = new UpdatesTracker(session);
            trackers.put(ProvisioningLayoutFactory.TRACK_UPDATES, updates);
        }
    }

    public static ProgressTracker<FPID> newFindTracker(PmCommandInvocation invoc) {
        FindTracker findTracker = new FindTracker(invoc.getPmSession());
        findTracker.commandStart(invoc);
        ProgressTracker<FPID> tracker = new DefaultProgressTracker<>(findTracker);
        return tracker;
    }
}
