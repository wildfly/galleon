/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractProvisionedPackageCommand;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractProvisionedPackageCommand extends CoreAbstractFPProvisionedCommand<AbstractProvisionedPackageCommand> {

    @Override
    public ProducerSpec getProducer(ProvisioningSession session, AbstractProvisionedPackageCommand command) throws CommandExecutionException {
        return getProducerSpec(session, command);
    }

    public static ProducerSpec getProducerSpec(ProvisioningSession session, AbstractProvisionedPackageCommand command) throws CommandExecutionException {
        if (command.getOrigin() == null) {
            return null;
        }
        try {
            return session.getResolvedLocation(null, command.getOrigin()).getProducer();
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(), CliErrors.retrieveProducerFailed(), ex);
        }
    }
    protected String getPackage(ProvisioningSession session, AbstractProvisionedPackageCommand command) throws CommandExecutionException {
        FeaturePackConfig cf = getProvisionedFP(session, command);
        Collection<String> pkgs = command.isIncludedPackages() ? cf.getIncludedPackages() : cf.getExcludedPackages();
        Set<String> packages = new HashSet<>();
        packages.addAll(pkgs);
        if (packages.contains(command.getPackage())) {
            return command.getPackage();
        }
        return null;
    }
}
