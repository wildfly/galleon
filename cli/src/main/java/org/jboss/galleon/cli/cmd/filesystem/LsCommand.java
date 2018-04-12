/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.state.StateInfoUtil;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.path.PathParser;

@CommandDefinition(name = "ls", description = "show the current [dir] or [fp node]")
public class LsCommand extends PmSessionCommand {

    @Argument(completer = FileAndNodeCompleter.class)
    private String path;

    @Override
    protected void runCommand(PmCommandInvocation session) throws CommandExecutionException {
        boolean fp = session.getPmSession().getCurrentPath() != null;
        if (path == null || path.isEmpty()) {
            if (fp) {
                path = session.getPmSession().getCurrentPath();
            } else {
                path = session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath();
            }
        }
        if (fp) {
            FeatureContainer container = session.getPmSession().getContainer();
            if (!path.startsWith("" + PathParser.PATH_SEPARATOR)) {
                path = session.getPmSession().getCurrentPath() + path;
            }
            try {
                StateInfoUtil.printContentPath(session, container, path);
            } catch (Exception ex) {
                throw new CommandExecutionException(ex);
            }
        } else {
            Path res = Paths.get(path);
            if (Files.isDirectory(res)) {
                try {
                    displayDirectory(res, session);
                } catch (IOException ex) {
                    throw new CommandExecutionException(ex.getLocalizedMessage(), ex);
                }
            } else if (Files.isRegularFile(res)) {
                displayFile(res, session);
            } else if (!Files.exists(res)) {
                session.getShell().writeln("ls: cannot access "
                        + res.toString() + ": No such file or directory");
            }
        }
    }

    private void displayDirectory(Path input, PmCommandInvocation shell) throws IOException {
        Files.list(input).forEach((p) -> {
            shell.println(p.getFileName().toString());
        });
    }

    private void displayFile(Path input, PmCommandInvocation shell) {
        shell.println(input.getFileName().toString());
    }

}
