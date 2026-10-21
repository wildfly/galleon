/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state.pkg.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractProvisionedPackageCommand;
import org.jboss.galleon.cli.core.GalleonCoreContentCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 *
 * @author jdenise@redhat.com
 */
public class CoreStateTargetedFPContentCompleter implements GalleonCoreContentCompleter<ProvisioningSession> {

    @Override
    public List<String> complete(PmCompleterInvocation invoc, ProvisioningSession context) {
        try {
            State session = context.getState();
            List<String> lst = new ArrayList<>();
            if (session != null) {
                AbstractProvisionedPackageCommand cmd = (AbstractProvisionedPackageCommand) invoc.getCommand();
                String pkg = cmd.getPackage();
                for (FeaturePackConfig fc : session.getConfig().getFeaturePackDeps()) {
                    Collection<String> pkgs = cmd.isIncludedPackages() ? fc.getIncludedPackages() : fc.getExcludedPackages();
                    Set<String> packages = new HashSet<>();
                    packages.addAll(pkgs);
                    if (packages.contains(pkg)) {
                        lst.add(Identity.buildOrigin(fc.getLocation().getProducer()));
                    }
                }
            }
            return lst;
        } catch (Exception ex) {
            CliLogging.completionException(ex);
            return Collections.emptyList();
        }
    }

}
