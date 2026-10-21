/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.creator.tasks;

import java.io.IOException;
import java.nio.file.Path;

import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
class PathCopy extends SrcPathTask {

    protected PathCopy(Path src, String relativeTarget) {
        this(src, relativeTarget, true);
    }

    protected PathCopy(Path src, String relativeTarget, boolean isContent) {
        super(src, relativeTarget, isContent);
    }
    /* (non-Javadoc)
     * @see org.jboss.galleon.test.util.fs.FsTask#execute(org.jboss.galleon.test.util.fs.FsTaskContext)
     */
    @Override
    public void execute(FsTaskContext ctx) throws IOException {
        IoUtils.copy(src, resolveTarget(ctx));
    }
}
