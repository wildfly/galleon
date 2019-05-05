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

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "resolve-feature-pack", description = HelpDescriptions.RESOLVE_FP)
public class MavenResolveFeaturePack extends PmSessionCommand {

    @Argument(completer = FPLocationCompleter.class, description = HelpDescriptions.LOCATION_FP_RESOLVE)
    protected String fpl;

    @Option(hasValue = false, description = HelpDescriptions.VERBOSE)
    private boolean verbose;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        try {
            if (verbose) {
                session.getPmSession().enableMavenTrace(true);
            }
            try {
                session.getPmSession().downloadFp(session.getPmSession().getResolvedLocation(null, fpl).getFPID());
                session.println("artifact installed in local mvn repository " + session.getPmSession().
                        getPmConfiguration().getMavenConfig().getLocalRepository());
            } finally {
                session.getPmSession().enableMavenTrace(false);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.resolveFeaturePackFailed(), ex);
        }
    }
}
