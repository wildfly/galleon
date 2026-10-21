/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
