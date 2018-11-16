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
package org.jboss.galleon.userchanges.persist.test;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.diff.FsEntry;
import org.jboss.galleon.diff.ProvisioningDiffProvider;
import org.jboss.galleon.plugin.StateDiffPlugin;
import org.jboss.galleon.xml.ProvisionedConfigXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicStateDiffPlugin implements StateDiffPlugin {

    private static final int ADDED = 1;
    private static final int MODIFIED = 2;
    private static final int REMOVED = 3;

    private static ConfigId resolveConfigId(FsEntry entry) {
        final String relativePath = entry.getRelativePath();
        final int pathLength = relativePath.length();
        if(pathLength < 9) {
            return null;
        }
        if(!relativePath.startsWith(Constants.CONFIGS)) {
            return null;
        }
        if(relativePath.charAt(7) != '/') {
            return null;
        }
        int i = relativePath.indexOf('/', 8);
        String model = null;
        String name = null;
        if(i < 0) {
            name = relativePath.substring(8);
        } else {
            model = relativePath.substring(8, i);
            if(i < pathLength - 1) {
                name = relativePath.substring(i + 1);
                if(name.indexOf('/') > 0) {
                    return null;
                }
            }
        }
        return new ConfigId(model, name);
    }

    @Override
    public void diff(ProvisioningDiffProvider diffProvider) throws ProvisioningException {
        final FsDiff fsDiff = diffProvider.getFsDiff();
        if(fsDiff.isEmpty()) {
            return;
        }
        if(fsDiff.hasAddedEntries()) {
            for(FsEntry entry : fsDiff.getAddedEntries()) {
                processEntry(entry, diffProvider, ADDED);
            }
        }
        if(fsDiff.hasModifiedEntries()) {
            for(FsEntry[] entries : fsDiff.getModifiedEntries()) {
                processEntry(entries[1], diffProvider, MODIFIED);
            }
        }
        if(fsDiff.hasRemovedEntries()) {
            for(FsEntry entry : fsDiff.getRemovedEntries()) {
                processEntry(entry, diffProvider, REMOVED);
            }
        }
    }

    private void processEntry(FsEntry entry, ProvisioningDiffProvider diffProvider, int status) throws ProvisioningException {
        if(entry.hasChildren()) {
            for(FsEntry child : entry.getChildren()) {
                processEntry(child, diffProvider, status);
            }
            return;
        }
        final ConfigId configId = resolveConfigId(entry);
        if(configId == null) {
            return;
        }
        switch(status) {
            case ADDED:
                configAdded(diffProvider, entry);
                break;
            case MODIFIED:
                configModified(diffProvider, entry);
                break;
            case REMOVED:
                configRemoved(diffProvider, configId, entry);
                break;
            default:
                throw new IllegalStateException("Unexpected status " + status);
        }
    }

    private void configAdded(ProvisioningDiffProvider diffProvider, FsEntry entry) throws ProvisioningException {
        diffProvider.addConfig(ProvisionedConfigXmlParser.parse(entry.getPath()), entry.getRelativePath());
    }

    private void configModified(ProvisioningDiffProvider diffProvider, FsEntry entry) throws ProvisioningException {
        diffProvider.updateConfig(ProvisionedConfigXmlParser.parse(entry.getPath()), entry.getRelativePath());
    }

    private void configRemoved(ProvisioningDiffProvider diffProvider, ConfigId configId, FsEntry entry) throws ProvisioningException {
        diffProvider.removeConfig(configId, entry.getRelativePath());
    }
}