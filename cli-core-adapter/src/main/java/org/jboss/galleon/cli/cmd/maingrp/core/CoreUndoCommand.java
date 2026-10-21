/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.maingrp.UndoCommand;
import org.jboss.galleon.cli.core.GalleonCoreDynamicExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;

/**
 *
 *
 * @author jdenise@redhat.com
 */
public class CoreUndoCommand implements GalleonCoreDynamicExecution<ProvisioningSession, UndoCommand> {

    protected ProvisioningManager getManager(ProvisioningSession session, UndoCommand command) throws ProvisioningException, IOException {
        return session.newProvisioningManager(command.getInstallationDirectory(session.getPmSession().getAeshContext()), command.isVerbose());
    }

    @Override
    public void execute(ProvisioningSession session, UndoCommand command, Map<String, String> options) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session, command);
            mgr.undo();
        } catch (ProvisioningException | IOException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.undoFailed(), ex);
        }
    }

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(ProvisioningSession session, UndoCommand cmd) throws Exception {
        return Collections.emptyList();
    }

}
