/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
