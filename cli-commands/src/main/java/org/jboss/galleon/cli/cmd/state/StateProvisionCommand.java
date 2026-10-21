/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aesh.command.CommandException;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionCommand;

/**
 * Dual command, applies in case of edit mode or not.
 *
 * @author jdenise@redhat.com
 */
public class StateProvisionCommand extends AbstractProvisionCommand {

    public StateProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    public GalleonCommandExecutionContext getGalleonContext(PmSession session) throws CommandException {
        return AbstractStateCommand.getGalleonExecutionContext(session);
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.PROVISION_STATE;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateProvisionCommand";
    }

    @Override
    protected boolean canComplete(PmSession pmSession) {
        return true;
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // NO-OP
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
