/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.core.CoreAbstractFPProvisionedCommand;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractProvisionedPackageCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateProvisionedPackageContentCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

    @Override
    public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
        try {
                List<String> packages = new ArrayList<>();
                AbstractProvisionedPackageCommand cmd = (AbstractProvisionedPackageCommand) invoc.getCommand();
                ProducerSpec spec = CoreAbstractProvisionedPackageCommand.getProducerSpec(context, cmd);
                FeaturePackConfig fp = CoreAbstractFPProvisionedCommand.getProvisionedFPConfig(spec, context, cmd);
                if (fp == null) {
                    // We want them all from all FP
                    for (FeaturePackConfig fc : context.getState().getConfig().getFeaturePackDeps()) {
                        Collection<String> pkgs = cmd.isIncludedPackages() ? fc.getIncludedPackages() : fc.getExcludedPackages();
                        for (String pkg : pkgs) {
                            if (!packages.contains(pkg)) {
                                packages.add(pkg);
                            }
                        }
                    }
                    for (FeaturePackConfig fc : context.getState().getConfig().getTransitiveDeps()) {
                        Collection<String> pkgs = cmd.isIncludedPackages() ? fc.getIncludedPackages() : fc.getExcludedPackages();
                        for (String pkg : pkgs) {
                            if (!packages.contains(pkg)) {
                                packages.add(pkg);
                            }
                        }
                    }
                } else {
                    Collection<String> pkgs = cmd.isIncludedPackages() ? fp.getIncludedPackages() : fp.getExcludedPackages();
                    for (String pkg : pkgs) {
                        if (!packages.contains(pkg)) {
                            packages.add(pkg);
                        }
                    }
                }
                return packages;
            } catch (Exception ex) {
                CliLogging.completionException(ex);
                return Collections.emptyList();
            }
    }

}
