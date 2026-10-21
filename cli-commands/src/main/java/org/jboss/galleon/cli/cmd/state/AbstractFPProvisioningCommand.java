/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;


import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFPProvisioningCommand extends AbstractStateCommand {

    @Argument(completer = FPLocationCompleter.class, required = true, description = HelpDescriptions.FP_LOCATION)
    protected String fpl;

    public String getFpl() {
        return fpl;
    }
}
