/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
