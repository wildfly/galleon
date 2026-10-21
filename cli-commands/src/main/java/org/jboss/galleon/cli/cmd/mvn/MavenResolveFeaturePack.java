/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.mvn;

import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;
import org.jboss.galleon.universe.FeaturePackLocation;

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

    public boolean isVerbose() {
        return verbose;
    }

    public String getFpl() {
        return fpl;
    }

    @Override
    protected String getCoreVersion(PmSession session) throws ProvisioningException {
        return session.getGalleonBuilder().getCoreVersion(FeaturePackLocation.fromString(fpl));
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.core.cmd.installation.CoreMavenResolveFeaturePack";
    }

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn;t be called");
    }
}
