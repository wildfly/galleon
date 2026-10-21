/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "set-snapshot-update-policy", description = HelpDescriptions.MVN_SET_DEFAULT_SNAPSHOT_POLICY)
public class MavenSetSnapshotPolicy extends PmSessionCommand {

    @Argument(completer = MavenAddRepository.UpdatePolicyCompleter.class,
            description = HelpDescriptions.MVN_UPDATE_POLICY, required = true)
    private String policy;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().setDefaultSnapshotPolicy(policy);
        } catch (ProvisioningException | XMLStreamException | IOException ex) {
            throw new CommandExecutionException(ex.getLocalizedMessage());
        }
    }
}
