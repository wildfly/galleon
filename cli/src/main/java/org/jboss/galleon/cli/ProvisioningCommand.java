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
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import static org.jboss.galleon.cli.AbstractStateCommand.DIR_OPTION_NAME;
import static org.jboss.galleon.cli.AbstractStateCommand.VERBOSE_OPTION_NAME;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ProvisioningCommand extends PmSessionCommand implements CommandWithInstallationDirectory {

    @Option(name = DIR_OPTION_NAME, completer = FileOptionCompleter.class, required = false,
            description = HelpDescriptions.INSTALLATION_DIRECTORY)
    protected String targetDirArg;

    @Option(name = VERBOSE_OPTION_NAME, shortName = 'v', hasValue = false,
            description = HelpDescriptions.VERBOSE)
    boolean verbose;

    @Override
    public Path getInstallationDirectory(AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return targetDirArg == null ? PmSession.getWorkDir(context) : workDir.resolve(targetDirArg);
    }

    protected ProvisioningManager getManager(PmCommandInvocation session) throws ProvisioningException {
        return session.getPmSession().newProvisioningManager(getInstallationDirectory(session.getAeshContext()), verbose);
    }
}
