/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import org.aesh.command.activator.CommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
public interface PmCommandActivator extends CommandActivator {

    void setPmSession(PmSession pm);

    PmSession getSession();
}
