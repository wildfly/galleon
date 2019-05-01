/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.maingrp;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.UniverseManager.UniverseVisitor;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.Headers;
import org.jboss.galleon.cli.cmd.Table;
import org.jboss.galleon.cli.cmd.state.StateInfoUtil;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "list-feature-packs", description = HelpDescriptions.LIST)
public class ListFeaturePacksCommand extends PmSessionCommand {

    private static final String NONE = "NONE";

    @Option(required = false, name = "universe", description = HelpDescriptions.LIST_UNIVERSE)
    private String fromUniverse;

    @Option(required = false, name = "all-frequencies", hasValue = false, description = HelpDescriptions.LIST_ALL_FREQUENCIES)
    private Boolean allFrequencies;

    @Override
    public void runCommand(PmCommandInvocation invoc)
            throws CommandExecutionException {
        Map<UniverseSpec, Table> tables = new HashMap<>();
        Map<UniverseSpec, Set<String>> exceptions = new HashMap<>();
        // Search for an installation in the context
        Path installation = null;
        try {
            installation = Util.lookupInstallationDir(invoc.getConfiguration().getAeshContext(), null);
        } catch (ProvisioningException ex) {
            // XXX OK, no installation.
        }
        Path finalPath = installation;
        UniverseVisitor visitor = new UniverseVisitor() {
            @Override
            public void visit(Producer<?> producer, FeaturePackLocation loc) {
                if (loc.getFrequency() == null) {
                    return;
                }
                if (allFrequencies || loc.getFrequency().equals(producer.getDefaultFrequency())) {
                    Table table = tables.get(loc.getUniverse());
                    if (table == null) {
                        table = new Table(Headers.PRODUCT, Headers.UPDATE_CHANNEL, Headers.LATEST_BUILD);
                        tables.put(loc.getUniverse(), table);
                    }
                    loc = invoc.getPmSession().getExposedLocation(finalPath, loc);
                    table.addLine(producer.getName(), StateInfoUtil.formatChannel(loc),
                            (loc.getBuild() == null ? NONE : loc.getBuild()));
                }
            }

            @Override
            public void exception(UniverseSpec spec, Exception ex) {
                Set<String> set = exceptions.get(spec);
                if (set == null) {
                    set = new HashSet<>();
                    exceptions.put(spec, set);
                }
                set.add(ex.getLocalizedMessage() == null
                        ? ex.getMessage() : ex.getLocalizedMessage());
            }
        };
        try {
            if (fromUniverse != null) {
                invoc.getPmSession().getUniverse().
                        visitUniverse(UniverseSpec.fromString(fromUniverse), visitor, true);
            } else {
                invoc.getPmSession().getUniverse().
                        visitAllUniverses(visitor, true, finalPath);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(invoc.getPmSession(),
                    CliErrors.resolvedUniverseFailed(), ex);
        }
        FindCommand.printExceptions(invoc, exceptions);
        for (Entry<UniverseSpec, Table> entry : tables.entrySet()) {
            Table table = entry.getValue();
            table.sort(Table.SortType.ASCENDANT);
            invoc.println(table.build());
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
