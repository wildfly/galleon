/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd.installation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.aesh.command.option.Option;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.Util;
import org.jboss.galleon.cli.cmd.CommandWithInstallationDirectory;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractInstallationCommand extends PmSessionCommand implements CommandWithInstallationDirectory {

    /**
     * @return the targetDirArg
     */
    public File getTargetDirArg() {
        return targetDirArg;
    }

    @Option(name = "dir", required = false,
            description = HelpDescriptions.INSTALLATION_DIRECTORY)
    private File targetDirArg;

    @Override
    public Path getInstallationDirectory(AeshContext context) {
        try {
            return Util.lookupInstallationDir(context, getTargetDirArg() == null ? null : getTargetDirArg().toPath());
        } catch (ProvisioningException ex) {
            return null;
        }
    }

    @Override
    protected String getCoreVersion(PmSession session) throws ProvisioningException {
        Path home = getInstallationDirectory(session.getAeshContext());
        if (home == null || !Files.exists(home)) {
            throw new ProvisioningException("Not a galleon installation " + targetDirArg);
        }
        Path prov = PathsUtils.getProvisioningXml(home);
        return session.getGalleonBuilder().getCoreVersion(prov);
    }
}
