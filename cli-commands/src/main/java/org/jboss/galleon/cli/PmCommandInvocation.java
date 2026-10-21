/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import java.util.List;
import org.aesh.command.invocation.CommandInvocation;

/**
 * A CommandInvocation instance base class that provides access to a PmSession.
 *
 * @author Alexey Loubyansky
 */
public abstract class PmCommandInvocation implements CommandInvocation {

    private final PmSession session;
    protected PmCommandInvocation(PmSession session) {
        this.session = session;
    }

    public PmSession getPmSession() {
        return session;
    }

    public String formatColumns(List<String> lst) {
        return Util.formatColumns(lst,
                getShell().size().getWidth(),
                getShell().size().getHeight());
    }
}
