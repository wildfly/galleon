/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

import org.aesh.command.CommandDefinition;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.config.mvn.MavenRemoteRepository;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "info", description = "Display maven config")
public class MavenInfo extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        session.println("Maven xml settings");
        if (session.getPmSession().getPmConfiguration().getMavenConfig().getSettings() == null) {
            session.println("No settings set");
        } else {
            session.println(session.getPmSession().getPmConfiguration().getMavenConfig().getSettings().normalize().toString());
        }
        session.println("Local repository");
        session.println(session.getPmSession().getPmConfiguration().getMavenConfig().getLocalRepository().normalize().toString());
        session.println("Remote repositories");
        if (session.getPmSession().getPmConfiguration().getMavenConfig().getRemoteRepositories().isEmpty()) {
            session.println("No remote repository configured");
        } else {
            for (MavenRemoteRepository rep : session.getPmSession().getPmConfiguration().getMavenConfig().getRemoteRepositories()) {
                session.println("repository " + rep.getName());
                session.println(" url=  " + rep.getUrl());
                session.println(" type= " + rep.getType());
            }
        }
    }

}
