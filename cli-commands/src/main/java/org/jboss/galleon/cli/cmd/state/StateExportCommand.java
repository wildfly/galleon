/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.state;

import java.io.File;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "export", description = HelpDescriptions.EXPORT_STATE)
public class StateExportCommand extends AbstractStateCommand {

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    @Argument(required = false,
            description = HelpDescriptions.EXPORT_FILE)
    private File file;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateExportCommand";
    }

}
