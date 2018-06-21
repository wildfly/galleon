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
package org.jboss.galleon.cli.cmd.universe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.UniverseManager;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.Universe;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1UniverseFactory;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "search", description = "Search the universe for products. e.g.: wildfly*")
public class UniverseSearchCommand implements Command<PmCommandInvocation> {

    private static final String DEFAULT = "";

    @Argument(required = true)
    private String pattern;

    @Override
    public CommandResult execute(PmCommandInvocation commandInvocation) throws CommandException, InterruptedException {
        // A list of universes, we could imagine having named universe to reference the default.
        List<UniverseSpec> specs = new ArrayList<>();
        UniverseManager mgr = commandInvocation.getPmSession().getUniverse();
        specs.add(mgr.getDefaultUniverseSpec());
        for (String name : mgr.getUniverseNames()) {
            specs.add(mgr.getUniverseSpec(name));
        }
        pattern = pattern.replaceAll("\\*", ".*");
        Pattern cPattern = Pattern.compile(pattern);
        Map<String, List<String>> producers = new HashMap<>();

        for (UniverseSpec spec : specs) {
            try {
                if (spec.getFactory().equals(LegacyGalleon1UniverseFactory.ID)) {
                    continue;
                }
                List<String> products = new ArrayList<>();
                Universe<? extends Producer> u = mgr.getUniverseResolver().getUniverse(spec);
                for (Producer<?> p : u.getProducers()) {
                    if (p.getName().contains(pattern)
                            || cPattern.matcher(p.getName()).matches()) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(p.getName()).append(":");
                        if (p.getChannels().size() > 1) {
                            builder.append("[");
                        }
                        Iterator<? extends Channel> it = p.getChannels().iterator();
                        while (it.hasNext()) {
                            Channel c = it.next();
                            builder.append(c.getName());
                            if (it.hasNext()) {
                                builder.append(", ");
                            }
                        }
                        if (p.getChannels().size() > 1) {
                            builder.append("]");
                        }
                        if (!p.getFrequencies().isEmpty()) {
                            builder.append("/");
                        }
                        if (p.getFrequencies().size() > 1) {
                            builder.append("[");
                        }
                        Iterator<String> it2 = p.getFrequencies().iterator();
                        while (it2.hasNext()) {
                            builder.append(it2.next());
                            if (it2.hasNext()) {
                                builder.append(", ");
                            }
                        }
                        if (p.getFrequencies().size() > 1) {
                            builder.append("]");
                        }
                        products.add(builder.toString());
                    }
                }
                if (!products.isEmpty()) {
                    String name;
                    if (spec.equals(mgr.getDefaultUniverseSpec())) {
                        if (!producers.containsKey(DEFAULT)) {
                            name = DEFAULT;
                        } else {
                            // A named universe equals to default.
                            name = mgr.getUniverseName(spec);
                        }
                    } else {
                        name = mgr.getUniverseName(spec);
                    }
                    producers.put(name, products);
                }
            } catch (ProvisioningException ex) {
                // ok, continue.
            }
        }
        if (producers.isEmpty()) {
            commandInvocation.println("No product found for pattern " + pattern);
        } else {
            for (Entry<String, List<String>> entry : producers.entrySet()) {
                String name = entry.getKey();
                if (name.equals(DEFAULT)) {
                    commandInvocation.println("Products found in default universe");
                } else {
                    commandInvocation.println("Products found in " + name + " universe");
                }
                for (String p : entry.getValue()) {
                    commandInvocation.println(" " + p);
                }
            }
        }

        return CommandResult.SUCCESS;
    }

}
