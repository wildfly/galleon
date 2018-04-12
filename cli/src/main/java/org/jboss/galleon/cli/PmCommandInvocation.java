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
package org.jboss.galleon.cli;

import java.io.IOException;
import java.io.PrintStream;
import org.aesh.command.CommandException;
import org.aesh.command.CommandNotFoundException;
import org.aesh.command.Executor;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.invocation.CommandInvocationConfiguration;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.shell.Shell;
import org.aesh.command.validator.CommandValidatorException;
import org.aesh.command.validator.OptionValidatorException;
import org.aesh.readline.AeshContext;
import org.aesh.readline.Prompt;
import org.aesh.readline.action.KeyAction;

/**
 * A CommandInvocation instance that provides access to a PmSession.
 *
 * @author Alexey Loubyansky
 */
public class PmCommandInvocation implements CommandInvocation {

    private final CommandInvocation delegate;

    private final PmSession session;
    private final PrintStream out;
    private final PrintStream err;

    PmCommandInvocation(PmSession session, PrintStream out, PrintStream err,
            CommandInvocation delegate) {
        this.session = session;
        this.out = out;
        this.err = err;
        this.delegate = delegate;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    public PmSession getPmSession() {
        return session;
    }

    @Override
    public Shell getShell() {
        return delegate.getShell();
    }

    @Override
    public void setPrompt(Prompt prompt) {
        delegate.setPrompt(prompt);
    }

    @Override
    public Prompt getPrompt() {
        return delegate.getPrompt();
    }

    @Override
    public String getHelpInfo(String commandName) {
        return delegate.getHelpInfo(commandName);
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public AeshContext getAeshContext() {
        return delegate.getAeshContext();
    }

    @Override
    public void print(String msg) {
        delegate.print(msg);
    }

    @Override
    public void println(String msg) {
        delegate.println(msg);
    }

    @Override
    public CommandInvocationConfiguration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public KeyAction input() throws InterruptedException {
        return delegate.input();
    }

    @Override
    public String inputLine() throws InterruptedException {
        return delegate.inputLine();
    }

    @Override
    public String inputLine(Prompt prompt) throws InterruptedException {
        return delegate.inputLine(prompt);
    }

    @Override
    public void executeCommand(String input) throws CommandNotFoundException, CommandLineParserException, OptionValidatorException, CommandValidatorException, CommandException, InterruptedException, IOException {
        delegate.executeCommand(input);
    }

    @Override
    public Executor<? extends CommandInvocation> buildExecutor(String line) throws CommandNotFoundException, CommandLineParserException, OptionValidatorException, CommandValidatorException, IOException {
        return delegate.buildExecutor(line);
    }

    @Override
    public void print(String msg, boolean paging) {
        delegate.print(msg, paging);
    }

    @Override
    public void println(String msg, boolean paging) {
        delegate.println(msg, paging);
    }
}
