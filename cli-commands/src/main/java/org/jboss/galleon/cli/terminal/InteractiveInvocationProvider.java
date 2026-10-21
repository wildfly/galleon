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
public class InteractiveInvocationProvider implements CommandInvocationProvider<PmCommandInvocation> {

    private final PmSession session;
    private final boolean paging;

    public InteractiveInvocationProvider(PmSession session, boolean paging) {
        this.session = session;
        this.paging = paging;
    }

    @Override
    public PmCommandInvocation enhanceCommandInvocation(CommandInvocation commandInvocation) {
        return new InteractivePmCommandInvocation(session, commandInvocation, paging);
    }
}
