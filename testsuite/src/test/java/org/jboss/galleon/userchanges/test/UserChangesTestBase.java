/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.userchanges.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.diff.FsDiff;
import org.jboss.galleon.universe.SingleUniverseTestBase;
import org.jboss.galleon.util.IoUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class UserChangesTestBase extends SingleUniverseTestBase {

    private List<String> output = new ArrayList<>();

    protected void writeContent(String relativePath, String content) {
        final Path target = installHome.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create parent directories for " + relativePath, e);
        }
        try(BufferedWriter writer = Files.newBufferedWriter(target)) {
            writer.write(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to " + relativePath, e);
        }
    }

    protected void recursiveDelete(String relativePath) {
        IoUtils.recursiveDelete(installHome.resolve(relativePath));
    }

    protected void mkdirs(String relativePath) {
        try {
            Files.createDirectories(installHome.resolve(relativePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directories " + relativePath, e);
        }
    }

    @Override
    public void main() throws Throwable {
        super.main();

        assertLog(expectedDiff());
    }

    protected MessageWriter getMessageWriter() {
        return new DefaultMessageWriter() {
            @Override
            public void verbose(Throwable cause, CharSequence message) {
                output.add(message.toString());
                super.verbose(cause, message);
            }

            @Override
            public void print(Throwable cause, CharSequence message) {
                output.add(message.toString());
                super.print(cause, message);
            }

            @Override
            public void error(Throwable cause, CharSequence message) {
                output.add(message.toString());
                super.error(cause, message);
            }

            @Override
            public boolean isVerboseEnabled() {
                return super.isVerboseEnabled();
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    protected List<String> expectedDiff() {
        return null;
    }

    private void assertLog(List<String> expected) {
        if (expected == null) {
            return;
        }

        List<String> diff = filterFsDiffOutput();

        assertThat(diff).containsExactlyInAnyOrderElementsOf(expected);
    }

    private List<String> filterFsDiffOutput() {
        boolean diffStarted = false;
        List<String> diff = new ArrayList<>();
        for (String s : output) {
            if (s.equals(FsDiff.REPLAYING_CHANGES)) {
                diffStarted = true;
                continue;
            }
            if (!diffStarted) {
                continue;
            }
            if (!s.startsWith(" ") && !s.startsWith("!")) {
                // end of diff
                break;
            }
            diff.add(s);
        }
        return diff;
    }
}