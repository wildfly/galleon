/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
