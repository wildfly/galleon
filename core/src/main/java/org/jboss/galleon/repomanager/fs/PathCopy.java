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
package org.jboss.galleon.repomanager.fs;

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
