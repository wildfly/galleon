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

package org.jboss.galleon.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FsDiff {

    public static FsDiff diff(FsEntry original, FsEntry other) throws ProvisioningException {
        return new FsDiff(original, other);
    }

    private final FsEntry original;
    private final FsEntry other;
    private Map<String, FsEntry> added = Collections.emptyMap();
    private Map<String, FsEntry> removed = Collections.emptyMap();
    private Map<String, FsEntry[]> modified = Collections.emptyMap();

    private FsDiff(FsEntry original, FsEntry other) throws ProvisioningException {
        this.original = original;
        this.other = other;
        doDiff(original, other);
    }

    private void doDiff(FsEntry originalEntry, FsEntry otherEntry) throws ProvisioningException {
        if(originalEntry.isDir() != otherEntry.isDir()) {
            removed = CollectionUtils.put(removed, originalEntry.getRelativePath(), originalEntry);
            added = CollectionUtils.put(added, otherEntry.getRelativePath(), otherEntry);
            return;
        }
        if(originalEntry.dir) {
            final Map<String, FsEntry> otherChildren = otherEntry.cloneChildren();
            if (originalEntry.hasChildren()) {
                for (FsEntry originalChild : originalEntry.getChildren()) {
                    final FsEntry otherChild = otherChildren.remove(originalChild.getName());
                    if(otherChild == null) {
                        removed = CollectionUtils.put(removed, originalChild.getRelativePath(), originalChild);
                        continue;
                    }
                    doDiff(originalChild, otherChild);
                }
                if (!otherChildren.isEmpty()) {
                    for (FsEntry otherChild : otherChildren.values()) {
                        added = CollectionUtils.put(added, otherChild.getRelativePath(), otherChild);
                    }
                }
            }
            return;
        }
        if(!Arrays.equals(originalEntry.getHash(), otherEntry.getHash())) {
            modified = CollectionUtils.put(modified, originalEntry.getRelativePath(), new FsEntry[] {originalEntry, otherEntry});
        }
    }

    public boolean isEmpty() {
        return modified.isEmpty() && added.isEmpty() && removed.isEmpty();
    }

    public boolean hasAddedEntries() {
        return !added.isEmpty();
    }

    public Collection<FsEntry> getAddedEntries() {
        return added.values();
    }

    public boolean hasRemovedEntries() {
        return !removed.isEmpty();
    }

    public Collection<FsEntry> getRemovedEntries() {
        return removed.values();
    }

    public boolean hasModifiedEntries() {
        return !modified.isEmpty();
    }

    public Collection<FsEntry[]> getModifiedEntries() {
        return modified.values();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        List<String> names = null;
        if(!added.isEmpty()) {
            buf.append("added: ");
            names = new ArrayList<>(added.keySet());
            Collections.sort(names);
            StringUtils.append(buf, names);
        }
        if(!removed.isEmpty()) {
            if(buf.length() > 1) {
                buf.append("; ");
            }
            buf.append("removed: ");
            if(names == null) {
                names = new ArrayList<>(removed.size());
            } else {
                names.clear();
            }
            names.addAll(removed.keySet());
            Collections.sort(names);
            StringUtils.append(buf, names);
        }
        if(!modified.isEmpty()) {
            if(buf.length() > 1) {
                buf.append("; ");
            }
            buf.append("modified: ");
            if(names == null) {
                names = new ArrayList<>(modified.size());
            } else {
                names.clear();
            }
            names.addAll(modified.keySet());
            Collections.sort(names);
            StringUtils.append(buf, names);
        }
        return buf.append(']').toString();
    }
}
