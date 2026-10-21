/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-repository", description = HelpDescriptions.MVN_REMOVE_REPO)
public class MavenRemoveRepository extends PmSessionCommand {

    public static class RepositoriesCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            List<String> names = new ArrayList<>();
            names.addAll(completerInvocation.getPmSession().getPmConfiguration().
                    getMavenConfig().getRemoteRepositoryNames());
            return names;
        }
    }

    @Argument(description = HelpDescriptions.MVN_REPO_NAME, required = true, completer = RepositoriesCompleter.class)
    private String name;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().removeRemoteRepository(name);
        } catch (ProvisioningException | XMLStreamException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.removeRepositoryFailed(), ex);
        }
    }
}
