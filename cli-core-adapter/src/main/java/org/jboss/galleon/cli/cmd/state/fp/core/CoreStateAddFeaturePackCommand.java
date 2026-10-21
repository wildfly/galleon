/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.fp.core;

import java.io.IOException;
import org.jboss.galleon.CoreVersion;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisioningCommand;
import org.jboss.galleon.cli.cmd.state.fp.StateAddFeaturePackCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.impl.VersionMatcher;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateAddFeaturePackCommand extends CoreAbstractFPProvisioningCommand<StateAddFeaturePackCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, StateAddFeaturePackCommand command, FeaturePackLocation fpl) throws IOException, ProvisioningException, CommandExecutionException {
        String vers = session.getPmSession().getGalleonBuilder().getCoreVersion(fpl);
        if (vers != null && !vers.isEmpty()) {
            if (VersionMatcher.COMPARATOR.compare(vers, CoreVersion.getVersion()) > 0) {
                throw new CommandExecutionException(CliErrors.stateAddFeaturePackVersionFailed());
            }
        }
        state.addDependency(session, command.getFpl(), fpl, command.isInheritConfigs(), command.isInheritPackages());
    }
}
