/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.featurepack;

import java.io.File;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.APIVersion;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.impl.ProvisioningUtil;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "import", description = HelpDescriptions.IMPORT_FP)
public class ImportCommand extends PmSessionCommand {

    @Argument(required = true, description = HelpDescriptions.FP_FILE_IMPORT)
    private File path;

    @Option(name = "install-in-universe", hasValue = true, required = false, description = HelpDescriptions.INSTALL_IN_UNIVERSE)
    private Boolean install;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't be called");
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.featurepack.core.CoreImportCommand";
    }

    @Override
    protected String getCoreVersion(PmSession session) throws ProvisioningException {
       String version = ProvisioningUtil.getFeaturePackDescription(getPath().toPath()).getGalleonVersion();
       if(version == null || version.isEmpty()) {
           version = APIVersion.getVersion();
       }
       return version;
    }

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    /**
     * @return the install
     */
    public Boolean getInstall() {
        return install;
    }
}
