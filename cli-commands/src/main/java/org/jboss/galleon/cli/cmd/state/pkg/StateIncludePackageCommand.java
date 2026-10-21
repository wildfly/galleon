/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.StateActivators.FPDependentCommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "include-package", description = HelpDescriptions.INCLUDE_PACKAGE, activator = FPDependentCommandActivator.class)
public class StateIncludePackageCommand extends AbstractPackageCommand {

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateIncludePackageCommand";
    }

}
