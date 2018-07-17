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
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.terminal.Key;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.StateCheckUpdatesCommand.Updates;
import static org.jboss.galleon.cli.cmd.state.StateCheckUpdatesCommand.ALL_DEPENDENCIES_OPTION_NAME;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "upgrade", description = "Upgrade the installation to latest available updates and patches")
public class StateUpgradeCommand extends org.jboss.galleon.cli.AbstractStateCommand {

    @Option(name = ALL_DEPENDENCIES_OPTION_NAME, hasValue = false, required = false)
    boolean includeAll;

    @Option(name = VERBOSE_OPTION_NAME, hasValue = false, required = false)
    boolean verbose;

    @Option(name = "yes", shortName = 'y', hasValue = false, required = false)
    boolean noConfirm;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session.getPmSession());
            Updates updates = StateCheckUpdatesCommand.getUpdatesTable(mgr, session, includeAll);
            if (updates.plan.isEmpty()) {
                session.println("Installation is up to date. No updates nor patches to apply.");
            } else {
                session.println("Some updates and/or patches have been found.");
                session.println(updates.t.build());
                if (!noConfirm) {
                    try {
                        Key k = null;
                        while (k == null || (!Key.y.equals(k) && !Key.n.equals(k))) {
                            session.print("Proceed with latest updates [y/n]?");
                            KeyAction a = session.input();
                            k = Key.findStartKey(a.buffer().array());
                        }
                        if (Key.n.equals(k)) {
                            return;
                        }
                    } finally {
                        session.println("");
                    }
                }
                mgr.apply(updates.plan);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(),
                    CliErrors.upgradeFailed(), ex);
        } catch (InterruptedException ignored) {
            // Just exit the command smoothly
        }
    }
}
