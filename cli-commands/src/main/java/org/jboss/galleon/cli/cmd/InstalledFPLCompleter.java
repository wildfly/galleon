/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCompleterInvocation;
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
        List<String> items = new ArrayList<>();
        try {
            GalleonCommandExecutionContext bridge = InstalledProducerCompleter.getCoreBridge(installation, completerInvocation.getPmSession());
            List<FeaturePackLocation> locations = bridge.getInstallationLocations(installation, false, true);

            for (FeaturePackLocation loc : locations) {
                items.add(bridge.getExposedLocation(installation, loc).toString());
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
            return Collections.emptyList();
        }
        return items;
    }

}
