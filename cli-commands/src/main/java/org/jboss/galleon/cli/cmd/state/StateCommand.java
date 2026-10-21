/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.GroupCommandDefinition;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.PmGroupCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(description = HelpDescriptions.STATE, name = "state", groupCommands = {StateEditCommand.class, StateNewCommand.class})
public class StateCommand implements PmGroupCommand {

    public static final String WELCOME_STATE_MSG = "Entering provisioning edit mode."
            + "Use 'add-dependency' command to add feature packs. Call 'leave-state' to leave this state.";

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.STATE_MODE;
    }

}
