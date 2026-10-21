/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.terminal;

import java.io.IOException;
import org.aesh.command.CommandException;
import org.aesh.command.CommandNotFoundException;
import org.aesh.command.Executor;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.invocation.CommandInvocationConfiguration;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.shell.Shell;
import org.aesh.command.validator.CommandValidatorException;
import org.aesh.command.validator.OptionValidatorException;
import org.aesh.readline.Prompt;
import org.aesh.readline.action.KeyAction;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
class CliShellPmCommandInvocation extends PmCommandInvocation {
    private final Shell shell;
    private final CommandInvocation delegate;

    CliShellPmCommandInvocation(PmSession session, Shell shell, CommandInvocation delegate) {
        super(session);
        this.shell = shell;
        this.delegate = delegate;
    }

    @Override
    public Shell getShell() {
        return shell;
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
    public String getHelpInfo(String string) {
        return delegate.getHelpInfo(string);
    }

    @Override
    public void stop() {
        // XXX jfdenise TODO!!!!
    }

    @Override
    public CommandInvocationConfiguration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public KeyAction input() throws InterruptedException {
        return shell.read();
    }

    @Override
    public String inputLine() throws InterruptedException {
        return shell.readLine();
    }

    @Override
    public String inputLine(Prompt prompt) throws InterruptedException {
        return shell.readLine(prompt);
    }

    @Override
    public void executeCommand(String string) throws CommandNotFoundException, CommandLineParserException, OptionValidatorException, CommandValidatorException, CommandException, InterruptedException, IOException {
        delegate.executeCommand(string);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Executor<? extends CommandInvocation> buildExecutor(String string) throws CommandNotFoundException, CommandLineParserException, OptionValidatorException, CommandValidatorException, IOException {
        return delegate.buildExecutor(string);
    }

    @Override
    public void print(String string, boolean bln) {
        shell.write(string, bln);
    }

    @Override
    public void println(String string, boolean bln) {
        shell.writeln(string, bln);
    }

    @Override
    public String getHelpInfo() {
        return delegate.getHelpInfo();
    }

}
