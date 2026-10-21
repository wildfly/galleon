/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "reset-settings-file", description = HelpDescriptions.MVN_RESET_SETTINGS_PATH)
public class MavenResetSettings extends PmSessionCommand {
    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().resetSettings();
        } catch (XMLStreamException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.setSettingsFailed(), ex);
        }
    }
}
