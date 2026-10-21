/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
