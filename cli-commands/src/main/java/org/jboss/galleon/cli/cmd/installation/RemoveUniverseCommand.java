/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.installation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-universe", description = HelpDescriptions.REMOVE_UNIVERSE)
public class RemoveUniverseCommand extends AbstractInstallationCommand {

    public static class UniverseCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            List<String> names = new ArrayList<>();
            CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
            Path installation = cmd.getInstallationDirectory(completerInvocation.
                    getAeshContext());
            GalleonCommandExecutionContext bridge;
            try {
                Path p = PathsUtils.getProvisioningXml(installation);
                String coreVersion = completerInvocation.getPmSession().getGalleonBuilder().getCoreVersion(p);
                bridge = completerInvocation.getPmSession().getGalleonContext(coreVersion);
                names.addAll(bridge.getUniverseNames(installation));
            } catch (CommandExecutionException | ProvisioningException ex) {
                CliLogging.completionException(ex);
                return Collections.emptyList();
            }
            return names;
        }
    }
    @Option(completer = UniverseCompleter.class, required = false, description = HelpDescriptions.UNIVERSE_NAME)
    private String name;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.installation.core.CoreRemoveUniverseCommand";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommandDomain getDomain() {
        return null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
