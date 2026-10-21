/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import org.aesh.command.activator.OptionActivator;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class PmOptionActivator implements OptionActivator {

    private PmSession pmSession;

    public PmSession getPmSession() {
        return pmSession;
    }

    void setPmSession(PmSession pmSession) {
        this.pmSession = pmSession;
    }
}
