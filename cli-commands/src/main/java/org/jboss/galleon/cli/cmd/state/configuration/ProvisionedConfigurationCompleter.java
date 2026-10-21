/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration;

import org.aesh.command.completer.OptionCompleter;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;

import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class ProvisionedConfigurationCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

    @Override
    public void complete(PmCompleterInvocation t) {
        t.getPmSession().getState().complete(this, t);
    }

    @Override
    public String getCoreCompleterClassName(PmSession session) {
        return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreProvisionedConfigurationCompleter";
    }

}
