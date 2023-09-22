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
