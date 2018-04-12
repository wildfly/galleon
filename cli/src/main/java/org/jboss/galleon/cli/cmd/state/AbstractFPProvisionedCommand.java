/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.state;

import java.io.IOException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ArtifactCoords.Ga;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFPProvisionedCommand extends AbstractStateCommand {

    public abstract Ga getGa(PmSession session) throws CommandExecutionException;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State session) throws IOException, ProvisioningException, CommandExecutionException {
        FeaturePackConfig cf = getProvisionedFP(invoc.getPmSession());
        runCommand(invoc, session, cf);
    }

    public FeaturePackConfig getProvisionedFP(PmSession session) throws CommandExecutionException {
        Ga ga = getGa(session);
        if (ga == null) {
            return null;
        }
        ProvisioningConfig config = session.getState().getConfig();
        for (FeaturePackConfig dep : config.getFeaturePackDeps()) {
            if (dep.getGav().toGa().equals(ga)) {
                return dep;
            }
        }
        return null;
    }

    protected abstract void runCommand(PmCommandInvocation invoc, State session,
            FeaturePackConfig config) throws IOException, ProvisioningException, CommandExecutionException;

}
