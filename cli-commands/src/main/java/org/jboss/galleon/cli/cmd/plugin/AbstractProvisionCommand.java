/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.plugin;

import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionCommand extends AbstractProvisionWithPlugins {

    public AbstractProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getName() {
        return "provision";
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }
}
