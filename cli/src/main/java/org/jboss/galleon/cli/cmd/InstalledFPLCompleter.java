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
package org.jboss.galleon.cli.cmd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.PmCompleterInvocation;
import static org.jboss.galleon.cli.cmd.InstalledProducerCompleter.getInstallationLocations;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 * Installed direct and patches completer.
 *
 * @author jdenise@redhat.com
 */
public class InstalledFPLCompleter extends AbstractCompleter {

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        Path installation = cmd.getInstallationDirectory(completerInvocation.
                getAeshContext());
        List<FeaturePackLocation> locations = getInstallationLocations(installation,
                completerInvocation.getPmSession(), false, true);
        List<String> items = new ArrayList<>();
        for (FeaturePackLocation loc : locations) {
            items.add(completerInvocation.getPmSession().
                    getExposedLocation(installation, loc).toString());
        }
        return items;
    }

}
