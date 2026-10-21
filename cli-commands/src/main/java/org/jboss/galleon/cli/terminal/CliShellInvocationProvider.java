/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.terminal;

import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.invocation.CommandInvocationProvider;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class CliShellInvocationProvider implements CommandInvocationProvider<PmCommandInvocation> {

    private final PmSession session;
    private final CliTerminalConnection connection;

    public CliShellInvocationProvider(PmSession session, CliTerminalConnection connection) {
        this.session = session;
        this.connection = connection;
    }

    @Override
    public PmCommandInvocation enhanceCommandInvocation(CommandInvocation commandInvocation) {
        return new CliShellPmCommandInvocation(session, connection.getShell(), commandInvocation);
    }
}
