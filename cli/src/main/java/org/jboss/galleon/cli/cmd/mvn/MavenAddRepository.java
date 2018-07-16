/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.cli.cmd.mvn;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import static org.jboss.galleon.cli.CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.config.mvn.MavenRemoteRepository;

/**
 * XXX TODO, jfdenise, we could add policies options to better configure the
 * repository.
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add-repository", description = "Add a maven repo")
public class MavenAddRepository extends PmSessionCommand {
    @Option(description = "Maven remote repository URL", required = true)
    private String url;

    @Option(description = "Maven remote repository type, \"" + DEFAULT_REPOSITORY_TYPE + "\" by default",
            required = false, defaultValue = DEFAULT_REPOSITORY_TYPE)
    private String type;

    @Option(description = "Maven remote repository name", required = true)
    private String name;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            session.getPmSession().getPmConfiguration().getMavenConfig().
                    addRemoteRepository(new MavenRemoteRepository(name, type, url));
        } catch (ProvisioningException | XMLStreamException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.addRepositoryFailed(), ex);
        }
    }

}
