/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;

@CommandDefinition(name = "ls", description = HelpDescriptions.LS)
public class StateLsCommand extends AbstractStateCommand {

    @Argument(completer = StateFullPathCompleter.class, description = HelpDescriptions.FP_PATH)
    private String path;

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateLsCommand";
    }
}
