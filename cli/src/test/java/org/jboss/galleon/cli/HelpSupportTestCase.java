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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.aesh.command.Command;
import org.aesh.command.CommandRuntime;
import org.aesh.command.impl.internal.ProcessedCommand;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.parser.CommandLineParser;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.registry.CommandRegistry;
import org.jboss.galleon.cli.config.Configuration;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class HelpSupportTestCase {

    // Check that all commands have descriptions
    @Test
    public void test() throws Exception {
        PmSession session = new PmSession(Configuration.parse());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandRuntime<? extends Command, ? extends CommandInvocation> runtime
                = CliMain.newRuntime(session, new PrintStream(out));
        CommandRegistry<? extends Command, ? extends CommandInvocation> registry
                = runtime.getCommandRegistry();
        for (String c : registry.getAllCommandNames()) {
            CommandLineParser<? extends Command> cmdParser = registry.getCommand(c, null).getParser();
            // XXX TODO jfdenise, to be removed when extensions are fixed.
            if (isExtension(cmdParser)) {
                continue;
            }
            checkCommand(cmdParser.getProcessedCommand(), true);
            if (cmdParser.isGroupCommand()) {
                for (CommandLineParser child : cmdParser.getAllChildParsers()) {
                    checkCommand(child.getProcessedCommand(), false);
                }
            }
        }
    }

    private boolean isExtension(CommandLineParser<? extends Command> cmdParser) {
        return cmdParser.getProcessedCommand().getCommand().getClass().getPackage().getName().startsWith("org.jboss.galleon.cli.cmd.filesystem");
    }

    private void checkCommand(ProcessedCommand<? extends Command> processedCommand, boolean parent) {
        if (!parent) {
            assertTrue(processedCommand.name(), processedCommand.description() != null && !processedCommand.description().isEmpty());
        }
        ProcessedOption arg = processedCommand.getArgument();
        if (arg != null) {
            assertTrue("Argument for " + processedCommand.name(), arg.description() != null && !arg.description().isEmpty());
        }
        ProcessedOption args = processedCommand.getArguments();
        if (args != null) {
            assertTrue("Arguments for " + processedCommand.name(), args.description() != null && !args.description().isEmpty());
        }
        for (ProcessedOption opt : processedCommand.getOptions()) {
            assertTrue(opt.name() + " for " + processedCommand.name(), opt.description() != null && !opt.description().isEmpty());
        }
    }

}
