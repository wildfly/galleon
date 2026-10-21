/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
