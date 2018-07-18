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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 * Installed producer completer.
 *
 * @author jdenise@redhat.com
 */
public class InstalledProducerCompleter extends AbstractCommaSeparatedCompleter {

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        CommandWithInstallationDirectory cmd = (CommandWithInstallationDirectory) completerInvocation.getCommand();
        Path currentDir = cmd.getInstallationDirectory(completerInvocation.getAeshContext());
        List<String> items = new ArrayList<>();
        try {
            ProvisioningManager.checkInstallationDir(currentDir);
            ProvisioningManager mgr = completerInvocation.getPmSession().
                    newProvisioningManager(currentDir, false);
            for (FeaturePackConfig fp : mgr.getProvisioningConfig().getFeaturePackDeps()) {
                items.add(completerInvocation.getPmSession().getExposedLocation(fp.getLocation()).getProducer().toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(InstalledProducerCompleter.class.getName()).log(Level.FINEST,
                    "Exception while completing: {0}", ex.getLocalizedMessage());
        }
        return items;
    }

}
