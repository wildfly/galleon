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
package org.jboss.galleon.cli.cmd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.PathsUtils;

/**
 * Installed producer and transitive dependencies completer.
 *
 * @author jdenise@redhat.com
 */
public class InstalledProducerCompleter extends AbstractCommaSeparatedCompleter {

    public static GalleonCommandExecutionContext getCoreBridge(Path installation, PmSession session) throws ProvisioningException, CommandExecutionException {
        Path p = PathsUtils.getProvisioningXml(installation);
        String coreVersion = session.getGalleonBuilder().getCoreVersion(p);
        return session.getGalleonContext(coreVersion);
    }

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        Path installation = cmd.getInstallationDirectory(completerInvocation.
                getAeshContext());
        List<String> items = new ArrayList<>();
        try {
            GalleonCommandExecutionContext bridge = getCoreBridge(installation, completerInvocation.getPmSession());
            List<FeaturePackLocation> locations = bridge.getInstallationLocations(installation, true, false);

            String trimed = completerInvocation.getGivenCompleteValue().trim();
            List<String> lst = trimed.isEmpty() ? Collections.emptyList()
                    : Arrays.asList(completerInvocation.getGivenCompleteValue().split(",+"));
            boolean ended = trimed.endsWith(",");
            int lastIndex = ended ? lst.size() : Math.max(0, lst.size() - 1);
            List<FeaturePackLocation> specified = new ArrayList<>();
            // List of specified locations.
            for (String s : lst) {
                specified.add(bridge.getResolvedLocation(installation, s));
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
                    items.add(bridge.getExposedLocation(installation, loc).getProducer().toString());
                }
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
            return Collections.emptyList();
        }
        return items;
    }

}
