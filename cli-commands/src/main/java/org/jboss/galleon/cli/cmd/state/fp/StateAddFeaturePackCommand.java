/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.fp;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisioningCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-dependency", description = HelpDescriptions.ADD_DEPENDENCY)
public class StateAddFeaturePackCommand extends AbstractFPProvisioningCommand {

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
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.fp.core.CoreStateAddFeaturePackCommand";
    }
    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
