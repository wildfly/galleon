/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "reset-release-update-policy", description = HelpDescriptions.MVN_RESET_DEFAULT_RELEASE_POLICY)
public class MavenResetReleasePolicy extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().resetDefaultReleasePolicy();
        } catch (ProvisioningException | XMLStreamException | IOException ex) {
            throw new CommandExecutionException(ex.getLocalizedMessage());
        }
    }
}
