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
package org.jboss.galleon.cli.tracking;

import org.aesh.utils.ANSI;
import org.aesh.utils.Config;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.progresstracking.ProgressCallback;
import org.jboss.galleon.progresstracking.ProgressTracker;

/**
 *
 * @author jdenise@redhat.com
 */
abstract class CliProgressTracker<T> implements ProgressCallback<T> {

    final String msgStart;
    final String msgComplete;
    PmCommandInvocation invocation;

    CliProgressTracker(String msgStart, String msgComplete) {
        this.msgStart = msgStart;
        this.msgComplete = msgComplete;
    }

    void commandStart(PmCommandInvocation invocation) {
        this.invocation = invocation;
    }

    void commandEnd(PmCommandInvocation invocation) {
        this.invocation = invocation;
    }

    void print(String content) {
        invocation.getShell().write(ANSI.CURSOR_SAVE);
        invocation.getShell().write(ANSI.ERASE_WHOLE_LINE);
        invocation.getShell().write(content);
        invocation.getShell().write(ANSI.CURSOR_RESTORE);
    }

    @Override
    public void starting(ProgressTracker<T> tracker) {
        print(msgStart);
    }

    // each time a new item is processed let's display it.
    // this seems to be the more efficient way to create a lively output.
    @Override
    public void processing(ProgressTracker<T> tracker) {
        String content = processingContent(tracker);
        if (content != null) {
            print(msgStart + " " + content);
        }
    }

    @Override
    public void pulse(ProgressTracker<T> tracker) {
        // NO-OP.
    }

    @Override
    public void complete(ProgressTracker<T> tracker) {
        if (msgComplete != null) {
            String content = completeContent(tracker);
            print(msgComplete + (content == null ? "" : " " + content) + Config.getLineSeparator());
        } else {
            // Simply erase the whole line, some content will come next
            // and will re-use the current line to print some content.
            invocation.getShell().write(ANSI.ERASE_WHOLE_LINE);
        }
    }

    protected abstract String processingContent(ProgressTracker<T> tracker);

    protected abstract String completeContent(ProgressTracker<T> tracker);

}
