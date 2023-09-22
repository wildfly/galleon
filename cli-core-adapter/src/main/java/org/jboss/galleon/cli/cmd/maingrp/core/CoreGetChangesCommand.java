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
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.maingrp.GetChangesCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsDiff.PathResolver;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreGetChangesCommand extends org.jboss.galleon.cli.cmd.installation.core.AbstractInstallationCommand<GetChangesCommand> {

    @Override
    public void execute(ProvisioningSession session, GetChangesCommand command) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session, command);
            FsDiff diff = mgr.getFsDiff();
            if (diff.isEmpty()) {
                session.getCommandInvocation().println("No changes detected");
            } else {
                Path workingDir = Paths.get(session.getCommandInvocation().getConfiguration().getAeshContext().
                        getCurrentWorkingDirectory().getAbsolutePath());
                Path installation = mgr.getInstallationHome();
                PathResolver resolver = new PathResolver() {
                    @Override
                    public String resolve(String relativePath) {
                        Path absPath = Paths.get(installation.toString(), relativePath);
                        return workingDir.relativize(absPath).toString();
                    }
                };
                FsDiff.log(diff, new Consumer<String>() {
                    @Override
                    public void accept(String msg) {
                        session.getCommandInvocation().println(msg);
                    }
                }, resolver);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(ex.getMessage());
        }
    }

}
