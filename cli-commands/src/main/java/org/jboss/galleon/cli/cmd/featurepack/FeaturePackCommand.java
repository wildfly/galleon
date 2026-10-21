/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.featurepack;

import org.aesh.command.GroupCommandDefinition;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.PmGroupCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(description = HelpDescriptions.FEATURE_PACK, name = "feature-pack", groupCommands = {ImportCommand.class,
    GetInfoCommand.class, ClearCacheCommand.class})
public class FeaturePackCommand implements PmGroupCommand {

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.FEATURE_PACK;
    }

}
