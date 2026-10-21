/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
