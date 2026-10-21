/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "set-local-repository", description = HelpDescriptions.MVN_SET_LOCAL_PATH)
public class MavenSetLocalRepository extends PmSessionCommand {

    @Argument(description = HelpDescriptions.MVN_LOCAL_REPO_PATH, required = true)
    private File path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        Path p = path.toPath();
        try {
            if (!Files.exists(p)) {
                throw new CommandExecutionException("Local repository directory " + p + " doesn't exist.");
            }
            session.getPmSession().getPmConfiguration().getMavenConfig().setLocalRepository(p);
        } catch (XMLStreamException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.setLocalRepositoryFailed(), ex);
        }
    }
}
