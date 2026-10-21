/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd.featurepack;

import java.util.Arrays;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.APIVersion;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import static org.jboss.galleon.cli.cmd.Headers.DEPENDENCIES;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.ALL;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.CONFIGS;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.LAYERS;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.OPTIONAL_PACKAGES;
import static org.jboss.galleon.cli.cmd.state.InfoTypeCompleter.OPTIONS;
import org.jboss.galleon.impl.ProvisioningUtil;
import org.jboss.galleon.universe.FeaturePackLocation;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "get-info", description = HelpDescriptions.GET_INFO_FP)
public class GetInfoCommand extends AbstractFeaturePackCommand {

    public static final String PATCH_FOR = "Patch for ";

    public static class InfoTypeCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            // No patch for un-customized FP.
            return Arrays.asList(ALL, CONFIGS, DEPENDENCIES, LAYERS, OPTIONAL_PACKAGES, OPTIONS);
        }

    }
    @Option(completer = InfoTypeCompleter.class, description = HelpDescriptions.FP_INFO_TYPE)
    private String type;

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't be called");
    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.featurepack.core.CoreGetInfoCommand";
    }

    @Override
    protected String getCoreVersion(PmSession session) throws ProvisioningException {
        if (getFpl() == null && getFile() == null) {
            return APIVersion.getVersion();
        }
        if (getFpl() != null) {
            FeaturePackLocation loc;
            try {
                loc = session.getGalleonContext(APIVersion.getVersion()).
                        getResolvedLocation(null, getFpl());
                return session.getGalleonBuilder().getCoreVersion(loc);
            } catch (CommandExecutionException ex) {
               throw new ProvisioningException(ex);
            }
        } else {
            String version = ProvisioningUtil.getFeaturePackDescription(getFile().toPath()).getGalleonVersion();
            if (version == null || version.isEmpty()) {
                version = APIVersion.getVersion();
            }
            return version;
        }
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
}
