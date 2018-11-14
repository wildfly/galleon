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
package org.jboss.galleon.cli.cmd.maingrp;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandDefinition;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.installation.AbstractInstallationCommand;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsDiff.PathResolver;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-changes", description = HelpDescriptions.GET_CHANGES)
public class GetChangesCommand extends AbstractInstallationCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(invoc.getPmSession());
            FsDiff diff = mgr.getFsDiff();
            if (diff.isEmpty()) {
                invoc.println("No changes detected");
            } else {
                Path workingDir = Paths.get(invoc.getConfiguration().getAeshContext().
                        getCurrentWorkingDirectory().getAbsolutePath());
                Path installation = mgr.getInstallationHome();
                PathResolver resolver = new PathResolver() {
                    @Override
                    public String resolve(String relativePath) {
                        Path absPath = Paths.get(installation.toString(), relativePath);
                        return workingDir.relativize(absPath).toString();
                    }
                };
                FsDiff.log(diff, invoc.getPmSession().getMessageWriter(false), resolver);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }
}
