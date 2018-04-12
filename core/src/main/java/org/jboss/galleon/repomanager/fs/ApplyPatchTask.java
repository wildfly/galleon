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
package org.jboss.galleon.repomanager.fs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class ApplyPatchTask extends RelativeTargetTask {
    private final List<String> diff;

    public ApplyPatchTask(String relativeTarget, List<String> diff) {
        super(relativeTarget);
        this.diff = diff;
    }

    @Override
    public void execute(FsTaskContext ctx) throws IOException {
        final Path target = resolveTarget(ctx);
        try {
            Patch<String> patch = DiffUtils.parseUnifiedDiff(diff);
            List<String> updatedLines = DiffUtils.patch(Files.readAllLines(target, StandardCharsets.UTF_8), patch);
            Files.write(target, updatedLines);
        } catch (PatchFailedException ex) {
            throw new IOException("Couldn't apply patch on " + target, ex);
        }
    }
}
