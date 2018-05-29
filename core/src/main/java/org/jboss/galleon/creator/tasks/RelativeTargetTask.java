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
