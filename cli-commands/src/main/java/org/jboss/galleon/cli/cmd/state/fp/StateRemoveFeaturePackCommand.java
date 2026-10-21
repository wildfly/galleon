/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.fp;

import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-dependency", description = HelpDescriptions.REMOVE_DEPENDENCY, activator = StateActivators.FPDependentCommandActivator.class)
public class StateRemoveFeaturePackCommand extends AbstractStateCommand {

    public static class ProvisionedFPCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.fp.core.CoreStateRemoveFeaturePackContentCompleter";
        }

    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.fp.core.CoreStateRemoveFeaturePackCommand";
    }

    @Argument(completer = ProvisionedFPCompleter.class, description = HelpDescriptions.FP_TO_REMOVE)
    private String fpl;

    /**
     * @return the fpl
     */
    public String getFpl() {
        return fpl;
    }

}
