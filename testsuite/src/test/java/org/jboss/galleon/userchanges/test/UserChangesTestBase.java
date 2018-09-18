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
package org.jboss.galleon.userchanges.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.galleon.universe.SingleUniverseTestBase;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class UserChangesTestBase extends SingleUniverseTestBase {

    protected void writeContent(String relativePath, String content) {
        try {
            Files.createDirectories(installHome.resolve(relativePath).getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create parent directories for " + relativePath, e);
        }
        try(BufferedWriter writer = Files.newBufferedWriter(installHome.resolve(relativePath))) {
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
}