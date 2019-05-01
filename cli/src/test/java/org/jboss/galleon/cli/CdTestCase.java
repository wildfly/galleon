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
package org.jboss.galleon.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.CommandException;
import org.aesh.command.CommandRuntime;
import org.jboss.galleon.cli.config.Configuration;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class CdTestCase {

    @Test
    public void test() throws Exception {
        PmSession session = new PmSession(Configuration.parse());
        session.throwException();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandRuntime runtime
                = CliMain.newRuntime(session, new PrintStream(out, false,
                        StandardCharsets.UTF_8.name()));
        Path current = Paths.get(session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath());
        runtime.executeCommand("cd");
        assertEquals(current, Paths.get(session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath()));

        File dir = Files.createTempDirectory("cd-test").toFile();
        dir.deleteOnExit();

        runtime.executeCommand("cd " + dir.getAbsolutePath());
        assertEquals(dir.toPath(), Paths.get(session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath()));

        runtime.executeCommand("cd -");
        assertEquals(current, Paths.get(session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath()));

        runtime.executeCommand("cd -");
        assertEquals(dir.toPath(), Paths.get(session.getAeshContext().getCurrentWorkingDirectory().getAbsolutePath()));

        try {
            runtime.executeCommand("cd foo");
            throw new Exception("Should have failed");
        } catch (CommandException ex) {
            // XXX OK
        }
    }
}
