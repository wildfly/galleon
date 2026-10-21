/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg.core;

import java.io.IOException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.pkg.StateExcludePackageCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateExcludePackageCommand extends CoreAbstractPackageCommand<StateExcludePackageCommand> {

    @Override
    protected void runCommand(ProvisioningSession session, State state, FeaturePackConfig config, StateExcludePackageCommand command) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            int i = command.getPackage().indexOf("/");
            String name = command.getPackage().substring(i + 1);
            // If the config is null, it means that the package exists in a transitive dependency
            // but no transitive dependency has been created yet.
            if (config == null) {
                state.excludePackageFromNewTransitive(session, getProducer(session, command), name);
            } else {
                state.excludePackage(session, name, config);
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.excludeFailed(), ex);
        }
    }
}
