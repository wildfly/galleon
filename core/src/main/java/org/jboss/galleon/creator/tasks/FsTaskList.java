/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.creator.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class FsTaskList implements FsTask {

    public static FsTaskList newList() {
        return new FsTaskList();
    }

    private List<FsTask> tasks = Collections.emptyList();

    private FsTaskList() {
    }

    public FsTaskList write(String content, String relativeTarget) {
        return write(content, relativeTarget, true);
    }

    public FsTaskList write(String content, String relativeTarget, boolean isContent) {
        return add(new StringToFile(content, relativeTarget, isContent));
    }

    public FsTaskList copy(Path src, String relativeTarget) {
        return copy(src, relativeTarget, true);
    }

    public FsTaskList copy(Path src, String relativeTarget, boolean isContent) {
        return add(new PathCopy(src, relativeTarget, isContent));
    }

    public FsTaskList copyDir(Path src, String relativeTarget, boolean contentOnly) {
        return copyDir(src, relativeTarget, contentOnly, true);
    }

    public FsTaskList copyDir(Path src, String relativeTarget, boolean contentOnly, boolean isContent) {
        return add(new DirCopy(src, relativeTarget, contentOnly, isContent));
    }

    public FsTaskList add(FsTask task) {
        tasks = CollectionUtils.add(tasks, task);
        return this;
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    @Override
    public void execute(FsTaskContext ctx) throws IOException {
        for(FsTask task : tasks) {
            task.execute(ctx);
        }
    }

    @Override
    public boolean isContent() {
        return false;
    }
}
