/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.installation.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.installation.SetHistoryLimitCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreSetHistoryLimitCommand extends AbstractInstallationCommand<SetHistoryLimitCommand> {

    @Override
    public void execute(ProvisioningSession context, SetHistoryLimitCommand command) throws CommandExecutionException {
        try {
            int s;
            try {
                s = Integer.parseInt(command.getLimit());
            } catch (NumberFormatException ex) {
                throw new CommandExecutionException(CliErrors.invalidHistoryLimit(command.getLimit()));
            }
            ProvisioningManager mgr = getManager(context, command);
            mgr.setStateHistoryLimit(s);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(context.getPmSession(), CliErrors.setHistoryLimitFailed(), ex);
        }
    }

}
