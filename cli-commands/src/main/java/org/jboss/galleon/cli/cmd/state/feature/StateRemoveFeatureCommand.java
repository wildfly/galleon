/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.feature;

import org.aesh.command.CommandDefinition;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators.FeatureCommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-feature", description = HelpDescriptions.REMOVE_FEATURE, activator = FeatureCommandActivator.class)
public class StateRemoveFeatureCommand extends AbstractStateCommand {

    public static class FeatureCompleter implements OptionCompleter<PmCompleterInvocation>, GalleonCLICommandCompleter {

        @Override
        public void complete(PmCompleterInvocation t) {
            t.getPmSession().getState().complete(this, t);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.feature.core.CoreFeatureCompleter";
        }

    }

    @Argument(required = true, completer = FeatureCompleter.class, description = HelpDescriptions.FEATURE_PATH)
    private String feature;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.feature.core.CoreStateRemoveFeatureCommand";
    }

    /**
     * @return the feature
     */
    public String getFeature() {
        return feature;
    }
}
