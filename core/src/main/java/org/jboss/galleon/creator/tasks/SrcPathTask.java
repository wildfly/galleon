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
abstract class SrcPathTask extends RelativeTargetTask {

    protected final Path src;

    protected SrcPathTask(Path src, String relativeTarget) {
        this(src, relativeTarget, true);
    }

    protected SrcPathTask(Path src, String relativeTarget, boolean isContent) {
        super(relativeTarget, isContent);
        this.src = src;
    }
}
