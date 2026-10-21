/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.StateAddUniverseCommand.UniverseFactoryCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-universe", description = HelpDescriptions.ADD_UNIVERSE)
public class AddUniverseCommand extends AbstractInstallationCommand {

    @Option(completer = UniverseFactoryCompleter.class, required = true, description = HelpDescriptions.UNIVERSE_FACTORY)
    private String factory;

    @Option(required = false, description = HelpDescriptions.UNIVERSE_NAME)
    private String name;

    @Option(required = true, description = HelpDescriptions.UNIVERSE_LOCATION)
    private String location;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.installation.core.CoreAddUniverseCommand";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommandDomain getDomain() {
        return null;
    }

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

}
