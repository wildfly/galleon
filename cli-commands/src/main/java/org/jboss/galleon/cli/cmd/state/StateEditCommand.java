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
package org.jboss.galleon.cli.cmd.state;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "edit", description = HelpDescriptions.EDIT_STATE)
public class StateEditCommand extends PmSessionCommand {

    @Argument(required = false,
            description = HelpDescriptions.EDIT_STATE_ARG)
    protected File dir;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't have been called");
    }
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateEditCommand";
    }
    public Path getInstallationHome(AeshContext context) {
        return dir == null ? PmSession.getWorkDir(context) : dir.toPath();
    }
    @Override
    public String getCoreVersion(PmSession session) throws ProvisioningException {
        Path toEdit = getInstallationHome(session.getAeshContext());
        Path provisioning = toEdit;
        if (Files.isDirectory(toEdit)) {
            provisioning = PathsUtils.getProvisioningXml(toEdit);
        }
        return session.getGalleonBuilder().getCoreVersion(provisioning);
    }

}
