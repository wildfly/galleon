/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractCommandActivator implements PmCommandActivator {

    private PmSession pm;

    protected AbstractCommandActivator() {
    }

    @Override
    public void setPmSession(PmSession pm) {
        this.pm = pm;
    }

    @Override
    public PmSession getSession() {
        return pm;
    }
}
