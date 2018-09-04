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
package org.jboss.galleon.cli.cmd.installation;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "set-history-limit", description = HelpDescriptions.SET_HISTORY_LIMIT)
public class SetHistoryLimitCommand extends AbstractInstallationCommand {
    // We can't rely on Integer injection due to AESH-479.
    @Argument(required = true, description = HelpDescriptions.HISTORY_LIMIT)
    private String limit;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            int s;
            try {
                s = Integer.parseInt(limit);
            } catch (NumberFormatException ex) {
                throw new CommandExecutionException(CliErrors.invalidHistoryLimit(limit));
            }
            getManager(invoc.getPmSession()).setStateHistoryLimit(s);
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.setHistoryLimitFailed(), ex);
        }
    }

}
