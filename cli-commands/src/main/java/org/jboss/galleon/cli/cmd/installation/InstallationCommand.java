/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.installation;

import org.aesh.command.GroupCommandDefinition;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.PmGroupCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(description = HelpDescriptions.INSTALLATION, name = "installation",
        groupCommands = {AddUniverseCommand.class, RemoveUniverseCommand.class,
            ClearHistoryCommand.class, SetHistoryLimitCommand.class,
            GetHistoryLimitCommand.class, ExportCommand.class})
public class InstallationCommand implements PmGroupCommand {

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.INSTALLATION;
    }

}
