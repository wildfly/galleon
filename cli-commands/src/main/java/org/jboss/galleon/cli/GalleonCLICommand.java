/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author jdenise
 */
public interface GalleonCLICommand {

    public String getCommandClassName(PmSession session) throws ProvisioningException ;

}
