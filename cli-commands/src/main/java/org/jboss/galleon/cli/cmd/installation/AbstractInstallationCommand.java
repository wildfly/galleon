/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
