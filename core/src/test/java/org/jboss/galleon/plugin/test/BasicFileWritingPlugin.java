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
package org.jboss.galleon.plugin.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.runtime.ProvisioningRuntime;
import org.jboss.galleon.util.IoUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class BasicFileWritingPlugin implements InstallPlugin {
    @Override
    public void postInstall(ProvisioningRuntime ctx) throws ProvisioningException {
        try {
            writeFile(ctx);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to write a file");
        }
    }

    protected abstract String getBasePath();

    protected abstract String getContent();

    protected void writeFile(ProvisioningRuntime ctx) throws IOException {
        writeFile(ctx, getBasePath(), getContent());
    }

    protected void writeFile(ProvisioningRuntime ctx, final String pathBase, String content) throws IOException {
        Path p = getTargetPath(ctx, pathBase);
        IoUtils.writeFile(p, content);
    }

    private Path getTargetPath(ProvisioningRuntime ctx, final String pathBase) {
        Path p = ctx.getStagedDir().resolve(pathBase);
        if (Files.exists(p)) {
            int i = 1;
            while (Files.exists(p)) {
                p = ctx.getStagedDir().resolve(pathBase + i++);
            }
        }
        return p;
    }
}