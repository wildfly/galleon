/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.tracking;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.core.ProvisioningSession;
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

    public static void registerTrackers(ProvisioningSession session) {
        init(session);
        for (Entry<String, CliProgressTracker<?>> entry : trackers.entrySet()) {
            session.getLayoutFactory().setProgressCallback(entry.getKey(), entry.getValue());
        }
    }

    public static void unregisterTrackers(ProvisioningSession session) {
        for (Entry<String, CliProgressTracker<?>> entry : trackers.entrySet()) {
            session.getLayoutFactory().setProgressCallback(entry.getKey(), null);
        }
    }

    public static void commandStart(ProvisioningSession session, PmCommandInvocation invoc) {
        init(session);
        for (CliProgressTracker<?> tracker : trackers.values()) {
            tracker.commandStart(invoc);
        }
    }

    public static void commandEnd(ProvisioningSession session, PmCommandInvocation invoc) {
        init(session);
        for (CliProgressTracker<?> tracker : trackers.values()) {
            tracker.commandEnd(invoc);
        }
    }

    private static void init(ProvisioningSession session) {
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

    public static ProgressTracker<FPID> newFindTracker(ProvisioningSession session, PmCommandInvocation invoc) {
        FindTracker findTracker = new FindTracker(session);
        findTracker.commandStart(invoc);
        ProgressTracker<FPID> tracker = new DefaultProgressTracker<>(findTracker);
        return tracker;
    }
}
