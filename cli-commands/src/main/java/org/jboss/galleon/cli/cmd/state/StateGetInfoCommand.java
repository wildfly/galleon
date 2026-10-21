/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-info", description = HelpDescriptions.GET_INFO_STATE)
public class StateGetInfoCommand extends AbstractStateCommand {

    @Option(completer = InfoTypeCompleter.class, description = HelpDescriptions.INFO_TYPE)
    private String type;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateGetInfoCommand";
    }

    public String getType() {
        return type;
    }
}
