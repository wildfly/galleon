/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.configuration;

import java.util.List;
import org.aesh.command.completer.OptionCompleter;
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
public abstract class AbstractDefaultConfigCommand extends AbstractFPProvisionedCommand {

    public static class ConfigCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

        @Override
        public void complete(PmCompleterInvocation t) {
            t.getPmSession().getState().complete(this, t);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreConfigCompleter";
        }

    }

    public static class TargetedFPCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.configuration.core.CoreAbstractDefaultConfigCommand$TargetedFPContentCompleter";
        }

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }
    }

    @Argument(required = true, description = HelpDescriptions.CONFIGURATION_FULL_NAME,
            completer = ConfigCompleter.class)
    private String configuration;

    @Option(completer = TargetedFPCompleter.class, description = HelpDescriptions.CONFIGURATION_ORIGIN)
    private String origin;

    public String getConfiguration() {
        return configuration;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

}
