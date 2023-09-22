/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.mvn.core;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.mvn.MavenResolveFeaturePack;
import org.jboss.galleon.cli.core.GalleonCoreExecution;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise
 */
public class CoreMavenResolveFeaturePack implements GalleonCoreExecution<ProvisioningSession, MavenResolveFeaturePack> {

    @Override
    public void execute(ProvisioningSession session, MavenResolveFeaturePack cmd) throws CommandExecutionException {
        if (cmd.isVerbose()) {
            session.getPmSession().enableMavenTrace(true);
        }
        try {
            session.downloadFp(session.getResolvedLocation(null, FeaturePackLocation.fromString(cmd.getFpl())).getFPID());
            session.getPmSession().println("artifact installed in local mvn repository " + session.getPmSession().
                    getPmConfiguration().getMavenConfig().getLocalRepository());
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), ex.getLocalizedMessage(), ex);
        } finally {
            session.getPmSession().enableMavenTrace(false);
        }
    }
}
