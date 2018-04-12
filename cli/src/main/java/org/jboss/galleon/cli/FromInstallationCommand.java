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

import java.nio.file.Path;
import org.aesh.command.option.Option;
import org.aesh.io.Resource;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.ProvisioningManager;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
abstract class FromInstallationCommand extends PmSessionCommand {

    @Option(name = "src", required = true,
            description = "Customized source installation directory.")
    protected Resource srcDirArg;

    @Option(name = "verbose", shortName = 'v', hasValue = false,
            description = "Whether or not the output should be verbose")
    boolean verbose;

    protected Path getTargetDir(PmCommandInvocation session) {
        Path workDir = PmSession.getWorkDir(session.getAeshContext());
        return srcDirArg == null ? workDir
                : workDir.resolve(srcDirArg.resolve(session.getAeshContext().
                        getCurrentWorkingDirectory()).get(0).getAbsolutePath());
    }

    protected ProvisioningManager getManager(PmCommandInvocation session) {
        return ProvisioningManager.builder()
                .setArtifactResolver(MavenArtifactRepositoryManager.getInstance())
                .setInstallationHome(getTargetDir(session))
                .setMessageWriter(new DefaultMessageWriter(session.getOut(),
                        session.getErr(), verbose))
                .build();
    }
}
