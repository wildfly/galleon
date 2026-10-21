/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import java.util.ArrayList;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.universe.UniverseFactoryLoader;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-universe", description = HelpDescriptions.ADD_UNIVERSE_STATE)
public class StateAddUniverseCommand extends AbstractStateCommand {

    public static class UniverseFactoryCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            List<String> names = new ArrayList<>();
            names.addAll(UniverseFactoryLoader.getInstance().getFactories());
            return names;
        }
    }
    @Option(completer = UniverseFactoryCompleter.class, required = true, description = HelpDescriptions.UNIVERSE_FACTORY)
    private String factory;

    @Option(required = false, description = HelpDescriptions.UNIVERSE_NAME)
    private String name;

    @Option(required = true, description = HelpDescriptions.UNIVERSE_LOCATION)
    private String location;

    /**
     * @return the factory
     */
    public String getFactory() {
        return factory;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateAddUniverseCommand";
    }

}
