/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import org.aesh.command.GroupCommandDefinition;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.PmGroupCommand;

/**
 *
 * @author jdenise@redhat.com
 */
@GroupCommandDefinition(description = HelpDescriptions.MAVEN, name = "maven", groupCommands = {MavenAddRepository.class,
    MavenRemoveRepository.class, MavenGetInfo.class, MavenSetLocalRepository.class, MavenResolveFeaturePack.class,
    MavenSetSettings.class, MavenSetReleasePolicy.class, MavenSetSnapshotPolicy.class,
    MavenEnableRelease.class, MavenEnableSnapshot.class, MavenEnableOffline.class,
    MavenResetLocalRepository.class, MavenResetOffline.class, MavenResetRelease.class,
    MavenResetReleasePolicy.class, MavenResetSettings.class, MavenResetSnapshot.class,
    MavenResetSnapshotPolicy.class})
public class MavenCommand implements PmGroupCommand {

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.CONFIGURATION;
    }

}
