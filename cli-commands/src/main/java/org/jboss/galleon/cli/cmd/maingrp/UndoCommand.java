/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.maingrp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins;

/**
 *
 *
 * @author jdenise@redhat.com
 */
public class UndoCommand extends AbstractProvisionWithPlugins {

    public UndoCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        return Collections.emptyList();
    }

    @Override
    protected String getName() {
        return "undo";
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't have been executed");
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.UNDO;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.maingrp.core.CoreUndoCommand";
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
    }

}
