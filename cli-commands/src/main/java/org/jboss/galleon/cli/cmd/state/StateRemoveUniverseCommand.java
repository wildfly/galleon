/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import java.util.ArrayList;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-universe", description = HelpDescriptions.REMOVE_UNIVERSE)
public class StateRemoveUniverseCommand extends AbstractStateCommand {

    public static class UniverseCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            List<String> names = new ArrayList<>();
            GalleonCommandExecutionContext ctx = completerInvocation.getPmSession().getState();
            names.addAll(ctx.getUniverseNames(null));
            return names;
        }
    }
    @Option(completer = UniverseCompleter.class, required = false, description = HelpDescriptions.UNIVERSE_NAME)
    private String name;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateRemoveUniverseCommand";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
