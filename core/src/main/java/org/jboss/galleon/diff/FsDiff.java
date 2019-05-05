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
package org.jboss.galleon.diff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.HashUtils;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FsDiff {

    private static class Change implements Comparable<Change> {

        private final String path;
        private final char tag;

        Change(String path, char tag) {
            this.path = path;
            this.tag = tag;
        }

        @Override
        public int compareTo(Change o) {
            return path.compareTo(o.path);
        }
    }

    public interface PathResolver {

        String resolve(String relativePath);
    }

    private static final char REPLAY_SKIP = 'S';
    private static final char ADDED = '+';
    private static final char MODIFIED = 'C'; // for conflict
    private static final char REMOVED = '-';

    public static final String CONFLICTS_WITH_THE_UPDATED_VERSION = "conflicts with the updated version";
    public static final String HAS_CHANGED_IN_THE_UPDATED_VERSION = "has changed in the updated version";
    public static final String HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION = "has been removed from the updated version";
    public static final String MATCHES_THE_UPDATED_VERSION = "matches the updated version";

    public static FsDiff diff(FsEntry original, FsEntry other) throws ProvisioningException {
        return new FsDiff(original, other);
    }

    public static Map<String, Boolean> replay(FsDiff diff, Path home, MessageWriter log) throws ProvisioningException {
        log.print("Replaying your changes on top");
        Map<String, Boolean> undoTasks = Collections.emptyMap();
        if(diff.hasRemovedEntries()) {
            for(FsEntry removed : diff.getRemovedEntries()) {
                if(removed.isDiffStatusSuppressed()) {
                    continue;
                }
                final Path target = home.resolve(removed.getRelativePath());
                if(Files.exists(target)) {
                    log.print(formatMessage(REMOVED, removed.getRelativePath(), null));
                    IoUtils.recursiveDelete(target);
                } else {
                    log.verbose(formatMessage(REMOVED, removed.getRelativePath(), HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION));
                    undoTasks = CollectionUtils.putLinked(undoTasks, removed.getRelativePath(), false);
                }
            }
        }
        if(diff.hasAddedEntries()) {
            for(FsEntry added : diff.getAddedEntries()) {
                if(added.isDiffStatusSuppressed()) {
                    continue;
                }
                undoTasks = addFsEntry(home, added, undoTasks, log);
            }
        }
        if(diff.hasModifiedEntries()) {
            for(FsEntry[] modified : diff.getModifiedEntries()) {
                if(modified[0].isDiffStatusSuppressed()) {
                    continue;
                }
                final FsEntry update = modified[1];
                final Path target = home.resolve(update.getRelativePath());
                char action = MODIFIED;
                String warning = null;
                if(Files.exists(target)) {
                    final byte[] targetHash;
                    try {
                        targetHash = HashUtils.hashPath(target);
                    } catch (IOException e) {
                        throw new ProvisioningException(Errors.hashCalculation(target), e);
                    }
                    if(Arrays.equals(update.getHash(), targetHash)) {
                        if(!modifiedPathMatchesExisting(update)) {
                            action = REPLAY_SKIP;
                        }
                        undoTasks = CollectionUtils.putLinked(undoTasks, update.getRelativePath(), true);
                    } else if (!Arrays.equals(modified[0].getHash(), targetHash)) {
                        if (modifiedPathUpdated(update)) {
                            warning = HAS_CHANGED_IN_THE_UPDATED_VERSION;
                            glnew(target);
                        } else {
                            action = REPLAY_SKIP;
                        }
                    } else if (modifiedPathConflict(update)) {
                        glnew(target);
                    } else {
                        action = REPLAY_SKIP;
                    }
                } else if(modifiedPathNotPresent(update)) {
                    warning = HAS_BEEN_REMOVED_FROM_THE_UPDATED_VERSION;
                    action = ADDED;
                } else {
                    action = REPLAY_SKIP;
                }
                if (action != REPLAY_SKIP) {
                    log.print(formatMessage(action, update.getRelativePath(), warning));
                    try {
                        IoUtils.copy(update.getPath(), target);
                    } catch (IOException e) {
                        throw new ProvisioningException(Errors.copyFile(update.getPath(), target), e);
                    }
                }
            }
        }
        return undoTasks;
    }

    private static Map<String, Boolean> addFsEntry(Path home, FsEntry added, Map<String, Boolean> undoTasks, MessageWriter log) throws ProvisioningException {
        final Path target = home.resolve(added.getRelativePath());
        char action = ADDED;
        String warning = null;
        if(Files.exists(target)) {
            if(added.isDir()) {
                for (FsEntry child : added.getChildren()) {
                    if(child.isDiffStatusSuppressed()) {
                        continue;
                    }
                    undoTasks = addFsEntry(home, child, undoTasks, log);
                }
                return undoTasks;
            }
            final byte[] targetHash;
            try {
                targetHash = HashUtils.hashPath(target);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.hashCalculation(target), e);
            }
            if(Arrays.equals(added.getHash(), targetHash)) {
                if(!addedPathMatchesExisting(added)) {
                    action = REPLAY_SKIP;
                } else {
                    warning = MATCHES_THE_UPDATED_VERSION;
                    action = MODIFIED;
                }
                undoTasks = CollectionUtils.putLinked(undoTasks, added.getRelativePath(), true);
            } else if(addedPathConflict(added) && !added.isDir()) {
                warning = CONFLICTS_WITH_THE_UPDATED_VERSION;
                glnew(target);
                action = MODIFIED;
            }
        }
        if (action != REPLAY_SKIP) {
            log.print(formatMessage(action, added.getRelativePath(), warning));
            try {
                IoUtils.copy(added.getPath(), target);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(added.getPath(), target), e);
            }
        }
        return undoTasks;
    }

    private static boolean addedPathMatchesExisting(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s added by the user matches the file from the updated version", userEntry.getRelativePath());
        return false;
    }

    private static boolean addedPathConflict(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s added by the user conflicts with the updated version", userEntry.getRelativePath());
        return true;
    }

    private static boolean modifiedPathMatchesExisting(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s modified by the user matches its updated version", userEntry.getRelativePath());
        return false;
    }

    private static boolean modifiedPathConflict(FsEntry userEntry) throws ProvisioningException {
        //log.verbose("%s modified by the user conflicts with its updated version", userEntry.getRelativePath());
        return true;
    }

    private static boolean modifiedPathUpdated(FsEntry userEntry) throws ProvisioningException {
        //log.print("WARN: %s original modified by the user has changed in the new version", userEntry.getRelativePath());
        return true;
    }

    private static boolean modifiedPathNotPresent(FsEntry userEntry) throws ProvisioningException {
        //log.print("WARN: " + userEntry.getRelativePath() + " modified by the user is not present in the updated version");
        return true;
    }

    private static String formatMessage(char action, String path, String warning) {
        final StringBuilder buf = new StringBuilder();
        buf.append(' ').append(action).append(' ').append(path);
        if(warning != null) {
            buf.setCharAt(0, '!');
            buf.append(' ').append(warning);
        }
        return buf.toString();
    }

    private static void glnew(final Path target) throws ProvisioningException {
        try {
            IoUtils.copy(target, target.getParent().resolve(target.getFileName() + Constants.DOT_GLNEW));
        } catch (IOException e) {
            throw new ProvisioningException("Failed to persist " + target.getParent().resolve(target.getFileName() + Constants.DOT_GLNEW), e);
        }
    }

    /**
     * Log an FsDiff content.
     *
     * @param diff The content to log.
     * @param log The logged content consumer.
     * @param resolver By default the relative path to the installation root
     * directory is displayed. If a resolver is provided, it will be called
     * prior to display paths. resolver can be null.
     */
    public static void log(FsDiff diff, Consumer<String> log, PathResolver resolver) {
        if (diff.isEmpty()) {
            return;
        }
        List<Change> changes = new ArrayList<>();
        if (diff.hasRemovedEntries()) {
            for (FsEntry entry : diff.getRemovedEntries()) {
                addEntries(entry, changes, REMOVED);
            }
        }

        if (diff.hasAddedEntries()) {
            for (FsEntry entry : diff.getAddedEntries()) {
                addEntries(entry, changes, ADDED);
            }
        }

        if (diff.hasModifiedEntries()) {
            for (FsEntry[] entry : diff.getModifiedEntries()) {
                addEntries(entry[0], changes, MODIFIED);
            }
        }
        Collections.sort(changes);
        for (Change c : changes) {
            String path = resolver == null ? c.path : resolver.resolve(c.path);
            log.accept(formatMessage(c.tag, path, null));
        }
    }

    private static void addEntries(FsEntry entry, List<Change> changes, char tag) {
        if (entry.hasChildren()) {
            for (FsEntry child : entry.getChildren()) {
                addEntries(child, changes, tag);
            }
        } else {
            changes.add(new Change(entry.getRelativePath(), tag));
        }
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
            if (originalEntry.hasChildren()) {
                final Map<String, FsEntry> otherChildren = otherEntry.cloneChildren();
                for (FsEntry originalChild : originalEntry.getChildren()) {
                    final FsEntry otherChild = otherChildren.remove(originalChild.getName());
                    if(otherChild == null) {
                        originalChild.diffRemoved();
                        removed = CollectionUtils.put(removed, originalChild.getRelativePath(), originalChild);
                        continue;
                    }
                    doDiff(originalChild, otherChild);
                }
                if (!otherChildren.isEmpty()) {
                    for (FsEntry otherChild : otherChildren.values()) {
                        otherChild.diffAdded();
                        added = CollectionUtils.put(added, otherChild.getRelativePath(), otherChild);
                    }
                }
            } else if(otherEntry.hasChildren()) {
                for (FsEntry otherChild : otherEntry.getChildren()) {
                    otherChild.diffAdded();
                    added = CollectionUtils.put(added, otherChild.getRelativePath(), otherChild);
                }
            }
            return;
        }
        if(!Arrays.equals(originalEntry.getHash(), otherEntry.getHash())) {
            originalEntry.diffModified();
            otherEntry.diffModified();
            modified = CollectionUtils.put(modified, originalEntry.getRelativePath(), new FsEntry[] {originalEntry, otherEntry});
        }
    }

    public FsEntry getOriginalRoot() {
        return original;
    }

    public FsEntry getOtherRoot() {
        return other;
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

    public Set<String> getAddedPaths() {
        return added.keySet();
    }

    public FsEntry getAddedEntry(String relativePath) {
        return added.get(relativePath);
    }

    public boolean hasRemovedEntries() {
        return !removed.isEmpty();
    }

    public Collection<FsEntry> getRemovedEntries() {
        return removed.values();
    }

    public Set<String> getRemovedPaths() {
        return removed.keySet();
    }

    public FsEntry getRemovedEntry(String relativePath) {
        return removed.get(relativePath);
    }

    public boolean hasModifiedEntries() {
        return !modified.isEmpty();
    }

    public Collection<FsEntry[]> getModifiedEntries() {
        return modified.values();
    }

    public Set<String> getModifiedPaths() {
        return modified.keySet();
    }

    public FsEntry[] getModifiedEntry(String relativePath) {
        return modified.get(relativePath);
    }

    public FsEntry getEntry(String relativePath) {
        final FsEntry[] fsEntries = modified.get(relativePath);
        if(fsEntries != null) {
            return fsEntries[1];
        }
        FsEntry fsEntry = added.get(relativePath);
        if(fsEntry == null) {
            fsEntry = removed.get(relativePath);
            if(fsEntry == null) {
                final String[] pathElements = relativePath.split("/");
                FsEntry originalEntry = original;
                FsEntry otherEntry = other;
                for(String name : pathElements) {
                    originalEntry = originalEntry == null ? null : originalEntry.getChild(name);
                    otherEntry = otherEntry == null ? null : otherEntry.getChild(name);
                    if(originalEntry == null && otherEntry == null) {
                        return null;
                    }
                }
                fsEntry = originalEntry == null ? otherEntry : originalEntry;
            }
        }
        return fsEntry;
    }

    public void suppress(String relativePath) throws ProvisioningException {
        final FsEntry[] fsEntries = modified.get(relativePath);
        if(fsEntries != null) {
            fsEntries[0].diffSuppress();
            fsEntries[1].diffSuppress();
            return;
        }
        final FsEntry entry = getEntry(relativePath);
        if(entry == null) {
            throw new ProvisioningException("Failed to locate " + relativePath + " in the diff");
        }
        entry.diffSuppress();
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
