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
package org.jboss.galleon.cli.cmd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.PathsUtils;

/**
 * Installed producer and transitive dependencies completer.
 *
 * @author jdenise@redhat.com
 */
public class InstalledProducerCompleter extends AbstractCommaSeparatedCompleter {

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        List<FeaturePackLocation> locations = getInstallationLocations(completerInvocation, true, false);
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        List<String> items = new ArrayList<>();
        String trimed = completerInvocation.getGivenCompleteValue().trim();
        List<String> lst = trimed.isEmpty() ? Collections.emptyList()
                : Arrays.asList(completerInvocation.getGivenCompleteValue().split(",+"));
        boolean ended = trimed.endsWith(",");
        int lastIndex = ended ? lst.size() : Math.max(0, lst.size() - 1);
        List<FeaturePackLocation> specified = new ArrayList<>();
        try {
            // List of specified locations.
            for (String s : lst) {
                specified.add(completerInvocation.getPmSession().
                        getResolvedLocation(cmd.getInstallationDirectory(completerInvocation.
                                getAeshContext()), s));
            }
            for (FeaturePackLocation loc : locations) {
                boolean found = false;
                for (int i = 0; i < lastIndex; i++) {
                    FeaturePackLocation s = specified.get(i);
                    if (s.getProducer().equals(loc.getProducer())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    items.add(completerInvocation.getPmSession().
                            getExposedLocation(cmd.getInstallationDirectory(completerInvocation.
                                    getAeshContext()), loc).getProducer().toString());
                }
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
            return Collections.emptyList();
        }
        return items;
    }

    public static List<FeaturePackLocation> getInstallationLocations(PmCompleterInvocation completerInvocation, boolean transitive, boolean patches) {
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        Path currentDir = cmd.getInstallationDirectory(completerInvocation.getAeshContext());
        List<FeaturePackLocation> items = new ArrayList<>();
        try {
            PathsUtils.assertInstallationDir(currentDir);
            ProvisioningManager mgr = completerInvocation.getPmSession().
                    newProvisioningManager(currentDir, false);
            try (ProvisioningLayout<FeaturePackLayout> layout = mgr.getLayoutFactory().newConfigLayout(mgr.getProvisioningConfig())) {
                for (FeaturePackLayout fp : layout.getOrderedFeaturePacks()) {
                    if (fp.isDirectDep() || (fp.isTransitiveDep() && transitive)) {
                        items.add(fp.getFPID().getLocation());
                    }
                    if (patches) {
                        List<FeaturePackLayout> appliedPatches = layout.getPatches(fp.getFPID());
                        for (FeaturePackLayout patch : appliedPatches) {
                            items.add(patch.getFPID().getLocation());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
        }
        return items;
    }

}
