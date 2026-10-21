/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "new", description = HelpDescriptions.NEW_STATE)
public class StateNewCommand extends PmSessionCommand {

    @Option(name="feature-pack-location", completer = FPLocationCompleter.class, required = false, description = HelpDescriptions.FP_LOCATION)
    protected String fpl;

    @Option(name = "default-configs-inherit", required = false, hasValue = false, description = HelpDescriptions.INCLUDE_DEFAULT_CONFIGS)
    Boolean inheritConfigs;

    @Option(name = "packages-inherit", required = false, hasValue = false, description = HelpDescriptions.INCLUDE_DEFAULT_PACKAGES)
    Boolean inheritPackages;

    public Boolean isInheritConfigs() {
        return inheritConfigs;
    }

    public Boolean isInheritPackages() {
        return inheritPackages;
    }

    public FeaturePackLocation getFpl() {
         if(fpl == null) {
            return null;
        }
        return FeaturePackLocation.fromString(fpl);
    }

    @Override
    protected String getCoreVersion(PmSession session) throws ProvisioningException {
        FeaturePackLocation loc = getFpl();
        if(loc == null) {
            return null;
        }
        if(loc.getUniverse() == null) {
            loc = new FeaturePackLocation(session.getUniverse().getBuiltinUniverseSpec(),
                    loc.getProducerName(),
                    loc.getChannelName(),
                    loc.getFrequency(),
                    loc.getBuild());
        }
        return session.getGalleonBuilder().getCoreVersion(loc);
    }

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't have been called");
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateNewCommand";
    }

}
