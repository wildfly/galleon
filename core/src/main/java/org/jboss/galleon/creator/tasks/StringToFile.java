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
package org.jboss.galleon.creator.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Alexey Loubyansky
 */
class StringToFile extends RelativeTargetTask {

    private final String content;
    private final boolean isContent;

     protected StringToFile(String content, String relativeTarget) {
        this(content, relativeTarget, true);
    }

    protected StringToFile(String content, String relativeTarget, boolean isContent) {
        super(relativeTarget);
        this.content = content;
        this.isContent = isContent;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.test.util.fs.FsTask#execute(org.jboss.galleon.test.util.fs.FsTaskContext)
     */
    @Override
    public void execute(FsTaskContext ctx) throws IOException {
        final Path target = resolveTarget(ctx);
        if(!Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
        }
        try(BufferedWriter writer = Files.newBufferedWriter(target)) {
            writer.write(content);
        }
    }

    @Override
    public boolean isContent() {
        return isContent;
    }
}
