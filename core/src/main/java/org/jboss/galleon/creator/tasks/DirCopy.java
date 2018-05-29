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
package org.jboss.galleon.creator.tasks;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
class DirCopy extends SrcPathTask {

    private final boolean contentOnly;

    DirCopy(Path src, String relativeTarget, boolean contentOnly, boolean isContent) {
        super(src, relativeTarget, isContent);
        this.contentOnly = contentOnly;
    }

    DirCopy(Path src, String relativeTarget, boolean contentOnly) {
        this(src, relativeTarget, contentOnly, true);
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.test.util.fs.FsTask#execute(org.jboss.galleon.test.util.fs.FsTaskContext)
     */
    @Override
    public void execute(FsTaskContext ctx) throws IOException {
        final Path target = resolveTarget(ctx);
        if(contentOnly) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
                for(Path p : stream) {
                    IoUtils.copy(p, target);
                }
            }
        } else {
            IoUtils.copy(src, target);
        }
    }
}
