/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg;

import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractPackageCommand extends AbstractFPProvisionedCommand {

    public static class PackageCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

        @Override
        public void complete(PmCompleterInvocation t) {
            t.getPmSession().getState().complete(this, t);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.pkg.core.CorePackageCompleter";
        }

    }

    @Argument(required = true, description = HelpDescriptions.PACKAGE_NAME,
            completer = PackageCompleter.class)
    private String pkg;

    public String getPackage() {
        return pkg;
    }
}
