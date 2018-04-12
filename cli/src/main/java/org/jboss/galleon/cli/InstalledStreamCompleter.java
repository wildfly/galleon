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
package org.jboss.galleon.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.FeaturePackConfig;

/**
 * Installed streams completer. XXX TODO, for now complete FP GAV.
 *
 * @author jdenise@redhat.com
 */
public class InstalledStreamCompleter extends AbstractCompleter {

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        ProvisioningCommand cmd = (ProvisioningCommand) completerInvocation.getCommand();
        Path currentDir = cmd.getTargetDir(completerInvocation.getAeshContext());
        List<String> items = new ArrayList<>();
        try {
            ProvisioningManager.checkInstallationDir(currentDir);
            ProvisioningManager mgr = ProvisioningManager.builder().setInstallationHome(currentDir).build();
            for (FeaturePackConfig fp : mgr.getProvisioningConfig().getFeaturePackDeps()) {
                if(fp.getGav().getVersion() == null) {
                    continue;
                }
                items.add(fp.getGav().toString());
            }
        } catch (Exception ex) {
            // not a proper installation
        }
        return items;
    }

}
