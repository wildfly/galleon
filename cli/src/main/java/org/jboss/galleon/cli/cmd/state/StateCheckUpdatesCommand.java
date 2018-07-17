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
package org.jboss.galleon.cli.cmd.state;

import java.util.ArrayList;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.AbstractStateCommand;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.cli.cmd.Table.Cell;
import org.jboss.galleon.layout.FeaturePackUpdatePlan;
import org.jboss.galleon.layout.ProvisioningPlan;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "check-updates", description = "Get available updates for an installation")
public class StateCheckUpdatesCommand extends AbstractStateCommand {

    public static final String UP_TO_DATE = "Installation is up to date. No available updates nor patches.";
    public static final String UPDATES_AVAILABLE = "Some updates and/or patches are available.";

    static class Updates {

        Table t;
        ProvisioningPlan plan;
    }

    private static final String NONE = "none";
    static final String ALL_DEPENDENCIES_OPTION_NAME = "include-all-dependencies";

    @Option(name = ALL_DEPENDENCIES_OPTION_NAME, hasValue = false, required = false)
    boolean includeAll;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session.getPmSession());
            Updates updates = getUpdatesTable(mgr, session, includeAll);
            if (updates.plan.isEmpty()) {
                session.println(UP_TO_DATE);
            } else {
                session.println(UPDATES_AVAILABLE);
                session.println(updates.t.build());
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(),
                    CliErrors.checkForUpdatesFailed(), ex);
        }

    }

    static Updates getUpdatesTable(ProvisioningManager mgr, PmCommandInvocation session, boolean includeAll) throws ProvisioningException {
        Table t;
        if (includeAll) {
            t = new Table(Headers.PRODUCT, Headers.VERSION, Headers.CURRENT_BUILD,
                    Headers.UPDATE, Headers.PATCHES, Headers.DEPENDENCY, Headers.UNIVERSE);
        } else {
            t = new Table(Headers.PRODUCT, Headers.VERSION, Headers.CURRENT_BUILD,
                    Headers.UPDATE, Headers.PATCHES, Headers.UNIVERSE);
        }
        ProvisioningPlan plan = mgr.getUpdates(includeAll);
        Updates updates = new Updates();
        updates.plan = plan;
        updates.t = t;
        if (plan.isEmpty()) {
            return updates;
        }

        for (FeaturePackUpdatePlan p : plan.getUpdates()) {
            FeaturePackLocation loc = p.getInstalledLocation();
            String update = p.hasNewLocation() ? p.getNewLocation().getBuild() : NONE;
            UniverseSpec u = session.getPmSession().getExposedLocation(loc).getUniverse();
            Cell patches = new Cell();
            if (p.hasNewPatches()) {
                for (FPID id : p.getNewPatches()) {
                    patches.addLine(id.getBuild());
                }
            } else {
                patches.addLine(NONE);
            }

            List<Cell> line = new ArrayList<>();
            line.add(new Cell(loc.getProducerName()));
            line.add(new Cell(loc.getChannelName()));
            line.add(new Cell(loc.getBuild()));
            line.add(new Cell(update));
            line.add(patches);
            if (includeAll) {
                line.add(new Cell(p.isTransitive() ? "Y" : "N"));
            }
            line.add(new Cell((u == null ? "default" : u.toString())));
            t.addCellsLine(line);
        }
        t.sort(Table.SortType.ASCENDANT);
        return updates;
    }

}
