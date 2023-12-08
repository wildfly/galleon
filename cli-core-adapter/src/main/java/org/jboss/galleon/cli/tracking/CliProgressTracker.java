/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import org.aesh.readline.terminal.impl.WinSysTerminal;
import org.aesh.utils.ANSI;
import org.aesh.utils.Config;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.progresstracking.ProgressCallback;
import org.jboss.galleon.progresstracking.ProgressTracker;

/**
 *
 * @author jdenise@redhat.com
 */
abstract class CliProgressTracker<T> implements ProgressCallback<T> {

    private interface Printer {

        void starting();

        void processing(String content);

        void complete(String content);
    }

    private class BasicPrinter implements Printer {

        @Override
        public void complete(String content) {
            if (content != null) {
                invocation.println(content);
            }
        }

        @Override
        public void starting() {
            invocation.println(msgStart + "...");
        }

        @Override
        public void processing(String content) {
        }
    }

    private class ANSIPrinter implements Printer {

        void print(String content) {
            invocation.getShell().write(ANSI.CURSOR_HIDE);
            invocation.getShell().write(ANSI.CURSOR_RESTORE);
            invocation.getShell().write(ANSI.ERASE_WHOLE_LINE);
            invocation.getShell().write(ANSI.CURSOR_SAVE);
            invocation.getShell().write(content);
            invocation.getShell().write(ANSI.CURSOR_SHOW);
        }

        @Override
        public void complete(String content) {
            if (content != null) {
                print(content + Config.getLineSeparator());
            } else {
                invocation.getShell().write(ANSI.ERASE_WHOLE_LINE);
            }
            invocation.getShell().write(ANSI.CURSOR_RESTORE);
        }

        @Override
        public void starting() {
            invocation.getShell().write(ANSI.CURSOR_SAVE);
            print(msgStart);
        }

        @Override
        public void processing(String content) {
            print(content);
        }
    }
    final String msgStart;
    final String msgComplete;
    PmCommandInvocation invocation;
    private final Printer printer;

    CliProgressTracker(ProvisioningSession session, String msgStart, String msgComplete) {
        this.msgStart = msgStart;
        this.msgComplete = msgComplete;
        if (Config.isWindows()) {
            if (WinSysTerminal.isVTSupported()) {
                printer = new ANSIPrinter();
            } else {
                printer = new BasicPrinter();
            }
        } else {
            if(session.getPmSession().isAnsiSupported()) {
                printer = new ANSIPrinter();
            } else {
                printer = new BasicPrinter();
            }
        }
    }

    void commandStart(PmCommandInvocation invocation) {
        this.invocation = invocation;
    }

    void commandEnd(PmCommandInvocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public void starting(ProgressTracker<T> tracker) {
        printer.starting();
    }

    // each time a new item is processed let's display it.
    // this seems to be the more efficient way to create a lively output.
    @Override
    public void processing(ProgressTracker<T> tracker) {
        String content = processingContent(tracker);
        if (content != null) {
            printer.processing(msgStart + " " + content);
        }
    }

    @Override
    public void pulse(ProgressTracker<T> tracker) {
        // NO-OP.
    }

    @Override
    public void complete(ProgressTracker<T> tracker) {
        String content = msgComplete;
        if (content != null) {
            String completedContent = completeContent(tracker);
            content = msgComplete + (completedContent == null ? "" : " " + completedContent);
        }
        printer.complete(content);
    }

    protected abstract String processingContent(ProgressTracker<T> tracker);

    protected abstract String completeContent(ProgressTracker<T> tracker);

}
