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
package org.jboss.galleon.cli.terminal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import org.aesh.command.shell.Shell;
import org.aesh.io.Encoder;
import org.aesh.readline.Prompt;
import org.aesh.readline.Readline;
import org.aesh.readline.action.ActionDecoder;
import org.aesh.readline.editing.EditModeBuilder;
import org.aesh.readline.terminal.Key;
import org.aesh.readline.tty.terminal.TerminalConnection;
import org.aesh.terminal.Attributes;
import org.aesh.terminal.Terminal;
import org.aesh.terminal.tty.Capability;
import org.aesh.terminal.tty.Size;
import org.aesh.utils.Config;
import org.jboss.galleon.cli.CliLogging;

/**
 * A shell to handle ANSI and reading input from terminal. This shell is
 * attached to a not started terminal connection that is started stopped when
 * input reading is required.
 *
 * @author jdenise@redhat.com
 */
class CliShell implements Shell {

    private final Terminal terminal;
    private final Consumer<int[]> stdOut;
    private final TerminalConnection connection;
    private final Readline readline;
    CliShell(TerminalConnection connection) throws IOException {
        this.connection = connection;
        this.terminal = connection.getTerminal();
        if (terminal.getCodePointConsumer() == null) {
            stdOut = new Encoder(Charset.defaultCharset(), this::write);
        } else {
            stdOut = terminal.getCodePointConsumer();
        }
        readline = new Readline(EditModeBuilder.builder().create());
    }

    public void close() throws IOException {
        terminal.close();
    }

    private void write(byte[] data) {
        try {
            terminal.output().write(data);
        } catch (IOException ex) {
            CliLogging.exception(ex);
        }
    }

    @Override
    public void write(String msg, boolean paging) {
        stdOut.accept(msg.codePoints().toArray());
    }

    @Override
    public void writeln(String msg, boolean paging) {
        stdOut.accept((msg + Config.getLineSeparator()).codePoints().toArray());
    }

    @Override
    public void write(int[] out) {
        stdOut.accept(out);
    }

    @Override
    public void write(char out) {
        stdOut.accept(new int[]{out});
    }

    @Override
    public String readLine() throws InterruptedException {
        return readLine(new Prompt(""));
    }

    @Override
    public String readLine(Prompt prompt) throws InterruptedException {
        final String[] out = {null};
        readline.readline(connection, prompt, event -> {
            out[0] = event;
            connection.stopReading();
        });
        try {
            connection.openBlocking();
        } finally {
            connection.setStdinHandler(null);
        }
        return out[0];
    }

    @Override
    public Key read() throws InterruptedException {
        ActionDecoder decoder = new ActionDecoder();
        final Key[] key = {null};
        Attributes attributes = connection.enterRawMode();
        try {
            connection.setStdinHandler(keys -> {
                decoder.add(keys);
                if (decoder.hasNext()) {
                    key[0] = Key.findStartKey(decoder.next().buffer().array());
                    connection.stopReading();
                }
            });
            try {
                connection.openBlocking();
            } finally {
                connection.setStdinHandler(null);
            }
        } finally {
            connection.setAttributes(attributes);
        }
        return key[0];
    }

    @Override
    public Key read(Prompt prompt) throws InterruptedException {
        return null;
    }

    @Override
    public boolean enableAlternateBuffer() {
        return connection.put(Capability.enter_ca_mode);
    }

    @Override
    public boolean enableMainBuffer() {
        return connection.put(Capability.exit_ca_mode);
    }

    @Override
    public Size size() {
        return connection.size();
    }

    @Override
    public void clear() {
        connection.put(Capability.clear_screen);
    }

}
