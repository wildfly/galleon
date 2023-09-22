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
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.util.ArrayList;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.cli.cmd.Table.Cell;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreCheckUpdatesCommand extends org.jboss.galleon.cli.cmd.installation.core.AbstractInstallationCommand<CheckUpdatesCommand> {

    static class Updates {

        Table t;
        ProvisioningPlan plan;
    }

    private static final String NONE = "none";
    static final String ALL_DEPENDENCIES_OPTION_NAME = "include-all-dependencies";

    @Override
    public void execute(ProvisioningSession session, CheckUpdatesCommand command) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session, command);

            Updates updates = getUpdatesTable(mgr, session, command.isIncludeAll(), command.getFp());
            if (updates.plan.isEmpty()) {
                session.getPmSession().println(CheckUpdatesCommand.UP_TO_DATE);
            } else {
                session.getPmSession().println(CheckUpdatesCommand.UPDATES_AVAILABLE);
                session.getPmSession().println(updates.t.build());
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(),
                    CliErrors.checkForUpdatesFailed(), ex);
        }

    }

    static Updates getUpdatesTable(ProvisioningManager mgr, ProvisioningSession session,
            boolean includeAll, String fp) throws ProvisioningException, CommandExecutionException {
        if (includeAll && fp != null) {
            throw new CommandExecutionException(CliErrors.onlyOneOptionOf(CheckUpdatesCommand.FP_OPTION_NAME,
                    ALL_DEPENDENCIES_OPTION_NAME));
        }
        ProvisioningPlan plan;
        if (fp == null) {
            plan = mgr.getUpdates(includeAll);
        } else {
            String[] split = fp.split(",+");
            List<ProducerSpec> resolved = new ArrayList<>();
            List<FeaturePackLocation> locs = new ArrayList<>();
            for (String producer : split) {
                FeaturePackLocation loc = session.
                        getResolvedLocation(mgr.getInstallationHome(),
                                producer);
                if (loc.hasBuild()) {
                    locs.add(loc);
                } else {
                    resolved.add(loc.getProducer());
                }
            }
            if (!resolved.isEmpty()) {
                ProducerSpec[] arr = new ProducerSpec[resolved.size()];
                plan = mgr.getUpdates(resolved.toArray(arr));
            } else {
                plan = ProvisioningPlan.builder();
            }
            if (!locs.isEmpty()) {
                addCustomUpdates(plan, locs, mgr);
            }
        }
        Updates updates = new Updates();
        updates.plan = plan;
        if (plan.isEmpty()) {
            return updates;
        }
        boolean hasPatches = false;
        for (FeaturePackUpdatePlan p : plan.getUpdates()) {
            if (p.hasNewPatches()) {
                hasPatches = true;
                break;
            }
        }
        List<String> headers = new ArrayList<>();
        headers.add(Headers.PRODUCT);
        headers.add(Headers.CURRENT_BUILD);
        headers.add(Headers.UPDATE);
        if (hasPatches) {
            headers.add(Headers.PATCHES);
        }
        if (includeAll) {
            headers.add(Headers.DEPENDENCY);
        }
        headers.add(Headers.UPDATE_CHANNEL);
        updates.t = new Table(headers);

        for (FeaturePackUpdatePlan p : plan.getUpdates()) {
            FeaturePackLocation loc = p.getInstalledLocation();
            String update = p.hasNewLocation() ? p.getNewLocation().getBuild() : NONE;
            Cell patches = null;
            if (hasPatches) {
                patches = new Cell();
                if (p.hasNewPatches()) {
                    for (FPID id : p.getNewPatches()) {
                        patches.addLine(id.getBuild());
                    }
                } else {
                    patches.addLine(NONE);
                }
            }

            List<Cell> line = new ArrayList<>();
            line.add(new Cell(loc.getProducerName()));
            line.add(new Cell(loc.getBuild()));
            line.add(new Cell(update));
            if (hasPatches) {
                line.add(patches);
            }
            if (includeAll) {
                line.add(new Cell(p.isTransitive() ? "Y" : "N"));
            }
            FeaturePackLocation newLocation = session.getExposedLocation(mgr.getInstallationHome(), p.getNewLocation());
            line.add(new Cell(Util.formatChannel(newLocation)));
            updates.t.addCellsLine(line);
        }
        updates.t.sort(Table.SortType.ASCENDANT);
        return updates;
    }

    private static void addCustomUpdates(ProvisioningPlan plan, List<FeaturePackLocation> custom, ProvisioningManager mgr) throws ProvisioningException {
        try (ProvisioningLayout<?> layout = mgr.getLayoutFactory().newConfigLayout(mgr.getProvisioningConfig())) {
            for (FeaturePackLocation loc : custom) {
                FeaturePackLayout fpl = layout.getFeaturePack(loc.getProducer());
                FeaturePackLocation current = fpl.getFPID().getLocation();
                FeaturePackUpdatePlan fpPlan = FeaturePackUpdatePlan.request(current, fpl.isTransitiveDep()).setNewLocation(loc).buildPlan();
                if (fpPlan.hasNewLocation()) {
                    plan.update(fpPlan);
                }
            }
        }
    }
}
