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
        List<FeaturePackLocation> locations = getInstallationLocations(completerInvocation, false, true);
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        List<String> items = new ArrayList<>();
        for (FeaturePackLocation loc : locations) {
            items.add(completerInvocation.getPmSession().
                    getExposedLocation(cmd.getInstallationDirectory(completerInvocation.
                            getAeshContext()), loc).toString());
        }
        return items;
    }

}
