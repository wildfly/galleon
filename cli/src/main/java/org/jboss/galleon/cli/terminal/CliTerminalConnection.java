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
package org.jboss.galleon.cli.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.aesh.command.shell.Shell;
import org.aesh.readline.tty.terminal.TerminalConnection;
import org.aesh.terminal.Connection;

/**
 *
 * @author jdenise@redhat.com
 */
public class CliTerminalConnection {
    private class CliOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            connection.stdoutHandler().accept(new int[]{b});
        }

    }

    private final TerminalConnection connection;
    private final PrintStream out;
    private final CliShell shell;

    public CliTerminalConnection() throws IOException {
        connection = new TerminalConnection();
        out = new PrintStream(new CliOutputStream(), false, StandardCharsets.UTF_8.name());
        shell = new CliShell(connection);
    }

    public Connection getConnection() {
        return connection;
    }

    public PrintStream getOutput() {
        return out;
    }

    public Shell getShell() {
        return shell;
    }

    public void close() {
        connection.close();
    }
}
