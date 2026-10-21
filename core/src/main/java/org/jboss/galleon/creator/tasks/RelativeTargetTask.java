/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.creator.tasks;

import java.nio.file.Path;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class RelativeTargetTask implements FsTask {

    private final String relativeTarget;
    private final boolean isContent;

    protected RelativeTargetTask(String relativeTarget) {
        this(relativeTarget, true);
    }

    protected RelativeTargetTask(String relativeTarget, boolean isContent) {
        this.relativeTarget = relativeTarget;
        this.isContent = isContent;
    }

    @Override
    public boolean isContent() {
        return isContent;
    }

    protected Path resolveTarget(FsTaskContext ctx) {
        return ctx.getTargetRoot(isContent()).resolve(relativeTarget);
    }
}
