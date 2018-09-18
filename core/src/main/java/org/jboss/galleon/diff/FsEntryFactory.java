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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.util.CollectionUtils;

/**
 * Factory that builds a representation of a filesystem branch
 * possibly filtering out one or more of its subbranches.
 *
 * NOTE: this class is *NOT* thread-safe!
 *
 * @author Alexey Loubyansky
 */
public class FsEntryFactory {

    private static class PathFilter {

        final String[] pathElements;
        final boolean relative;
        final String suffix;

        final FsEntry[] checkEntries;

        PathFilter(String[] pathElements, boolean relative) {
            this.pathElements = pathElements;
            this.relative = relative;
            if(pathElements.length > 0) {
                String tmp = pathElements[pathElements.length - 1];
                if(tmp.charAt(0) == '*') {
                    tmp = tmp.substring(1);
                }
                suffix = tmp;
            } else {
                suffix = null;
            }
            this.checkEntries = pathElements.length - 1 <= 0 ? null : new FsEntry[pathElements.length - 1];
        }

        boolean matches(FsEntry parent, String childName) {
            if(checkEntries != null) {
                Arrays.fill(checkEntries, null);
            }
            if(relative) {
                if(pathElements.length > parent.depth + 1) {
                    return false;
                }
                int i = pathElements.length - 1;
                if(i > 0) {
                    FsEntry checkEntry = parent;
                    do {
                        if(checkEntry == null) {
                            return false;
                        }
                        checkEntries[i - 1] = checkEntry;
                        checkEntry = checkEntry.parent;
                        --i;
                    } while(i > 0);
                }
            } else if(pathElements.length == parent.depth + 1) {
                if(parent.depth > 0) {
                    FsEntry checkEntry = parent;
                    int i = checkEntries.length - 1;
                    checkEntries[i] = checkEntry;
                    while(checkEntry.depth > 1) {
                        checkEntry = checkEntry.parent;
                        checkEntries[--i] = checkEntry;
                    }
                }
            } else {
                return false;
            }

            if(pathElements.length == 0) {
                return true;
            }
            int i = 0;
            while(i < pathElements.length - 1) {
                if(!checkEntries[i].name.equals(pathElements[i++])) {
                    return false;
                }
            }
            if(!childName.endsWith(suffix)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            if(!relative) {
                buf.append('/');
            }
            if(pathElements.length > 0) {
                buf.append(pathElements[0]);
                for(int i = 1; i < pathElements.length; ++i) {
                    buf.append('/').append(pathElements[i]);
                }
            }
            return buf.toString();
        }
    }

    public static FsEntryFactory getInstance() {
        return new FsEntryFactory();
    }

    private List<PathFilter> pathFilters = Collections.emptyList();

    private FsEntryFactory() {
    }

    public FsEntry forPath(Path p) throws ProvisioningException {
        final FsEntry entry = new FsEntry(null, p);
        if(entry.dir) {
            try {
                initChildren(entry);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.fsEntryInit(p), e);
            }
        }
        return entry;
    }

    private void initChildren(final FsEntry parent) throws IOException {
        boolean hasDirs = false;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(parent.p)) {
            for(Path c : stream) {
                if(!pathFilters.isEmpty() && isFiltered(parent, c.getFileName().toString())) {
                    continue;
                }
                hasDirs |= new FsEntry(parent, c).dir;
            }
        }
        if(hasDirs) {
            for(FsEntry child : parent.getChildren()) {
                if(!child.dir) {
                    continue;
                }
                initChildren(child);
            }
        }
    }

    private boolean isFiltered(FsEntry parent, String childName) {
        if(pathFilters.isEmpty()) {
            return false;
        }
        for(PathFilter filter : pathFilters) {
            if(filter.matches(parent, childName)) {
                return true;
            }
        }
        return false;
    }

    public FsEntryFactory filter(String pathExpr) {
        final String[] entries;
        boolean relative = true;
        if(pathExpr.isEmpty()) {
            relative = false;
            entries = new String[0];
        } else if(pathExpr.charAt(0) == '/') {
            relative = false;
            entries = pathExpr.substring(1).split("/");
        } else {
            entries = pathExpr.split("/");
        }
        pathFilters = CollectionUtils.add(pathFilters, new PathFilter(entries, relative));
        return this;
    }
}
