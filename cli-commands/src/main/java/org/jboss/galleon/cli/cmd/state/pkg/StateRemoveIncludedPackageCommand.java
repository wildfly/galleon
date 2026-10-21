/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg;

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.StateActivators;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-included-package", description = HelpDescriptions.REMOVE_INCLUDED_PACKAGE, activator = StateActivators.ProvisionedPackageCommandActivator.class)
public class StateRemoveIncludedPackageCommand extends AbstractProvisionedPackageCommand {
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateRemoveIncludedPackageCommand";
    }
    @Override
    public boolean isIncludedPackages() {
        return true;
    }
}
