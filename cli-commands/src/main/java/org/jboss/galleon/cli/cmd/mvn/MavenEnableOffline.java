/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.BooleanCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "enable-offline", description = HelpDescriptions.MVN_ENABLE_OFFLINE)
public class MavenEnableOffline extends PmSessionCommand {

    @Argument(completer = BooleanCompleter.class, description = HelpDescriptions.BOOLEAN_OPT, required = true)
    private String offline;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().enableOffline(BooleanCompleter.validateValue(offline));
        } catch (XMLStreamException | IOException ex) {
            throw new CommandExecutionException(ex.getLocalizedMessage());
        }
    }
}
