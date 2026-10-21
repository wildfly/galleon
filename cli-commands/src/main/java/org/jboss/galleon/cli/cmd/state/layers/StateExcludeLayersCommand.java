/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.layers;

import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.AbstractCommaSeparatedCompleter;
import org.jboss.galleon.cli.cmd.state.StateActivators.ConfigDependentCommandActivator;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "exclude-layers", description = HelpDescriptions.EXCLUDE_LAYERS, activator = ConfigDependentCommandActivator.class)
public class StateExcludeLayersCommand extends AbstractLayersCommand {

    public static class LayersCompleter extends AbstractCommaSeparatedCompleter implements GalleonCLICommandCompleter {

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.layers.core.CoreStateExcludeLayersCommand$LayersCompleter";
        }

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.layers.core.CoreStateExcludeLayersCommand";
    }

    @Option(required = true, description = HelpDescriptions.EXCLUDE_LAYERS, completer = LayersCompleter.class)
    private String layers;

    /**
     * @return the layers
     */
    public String getLayers() {
        return layers;
    }
}
