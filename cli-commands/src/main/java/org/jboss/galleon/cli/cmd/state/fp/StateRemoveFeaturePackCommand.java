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
package org.jboss.galleon.cli.cmd.state.fp;

import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.cmd.state.StateActivators;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-dependency", description = HelpDescriptions.REMOVE_DEPENDENCY, activator = StateActivators.FPDependentCommandActivator.class)
public class StateRemoveFeaturePackCommand extends AbstractStateCommand {

    public static class ProvisionedFPCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.fp.core.CoreStateRemoveFeaturePackContentCompleter";
        }

    }

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.fp.core.CoreStateRemoveFeaturePackCommand";
    }

    @Argument(completer = ProvisionedFPCompleter.class, description = HelpDescriptions.FP_TO_REMOVE)
    private String fpl;

    /**
     * @return the fpl
     */
    public String getFpl() {
        return fpl;
    }

}
