/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg;

import java.util.List;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionedPackageCommand extends AbstractFPProvisionedCommand {

    public static class ProvisionedPackageCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateProvisionedPackageContentCompleter";
        }


    }
    public static class TargetedFPCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {
        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateTargetedFPContentCompleter";
        }
    }
    @Argument(required = true, description = HelpDescriptions.PACKAGE_NAME,
            completer = ProvisionedPackageCompleter.class)
    private String pkg;

    @Option(completer = TargetedFPCompleter.class, description = HelpDescriptions.PACKAGE_ORIGIN)
    private String origin;

    public String getPackage() {
        return pkg;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    public abstract boolean isIncludedPackages();
}
