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
 * Used by tests.
 *
 * @author jdenise@redhat.com
 */
public class OutputInvocationProvider implements CommandInvocationProvider<PmCommandInvocation> {

    private final PmSession session;

    public OutputInvocationProvider(PmSession session) {
        this.session = session;
    }

    @Override
    public PmCommandInvocation enhanceCommandInvocation(CommandInvocation commandInvocation) {
        return new OutputPmCommandInvocation(session, commandInvocation);
    }
}
