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
package org.jboss.galleon.cli.cmd.state.feature;

import java.io.IOException;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Argument;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.StateFullPathCompleter;
import org.jboss.galleon.cli.cmd.state.AbstractStateCommand;
import org.jboss.galleon.cli.model.ConfigInfo;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "remove-feature", description = HelpDescriptions.REMOVE_FEATURE, activator = FeatureCommandActivator.class)
public class StateRemoveFeatureCommand extends AbstractStateCommand {

    public static class FeatureCompleter extends StateFullPathCompleter {

        @Override
        protected String getCurrentPath(PmCompleterInvocation session) {
            return FeatureContainerPathConsumer.FINAL_CONFIGS_PATH;
        }
    }

    @Argument(required = true, completer = FeatureCompleter.class, description = HelpDescriptions.FEATURE_PATH)
    private String feature;

    @Override
    protected void runCommand(PmCommandInvocation invoc, State session) throws IOException, ProvisioningException, CommandExecutionException {
        try {
            String path = FeatureContainerPathConsumer.FINAL_CONFIGS_PATH
                    + (feature.endsWith("" + PathParser.PATH_SEPARATOR) ? feature : feature + PathParser.PATH_SEPARATOR);
            FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(session.getContainer(), false);
            PathParser.parse(path, consumer);
            ConfigInfo ci = consumer.getConfig();
            if (ci == null) {
                throw new ProvisioningException("Not a valid configuration " + feature);
            }
            Group grp = consumer.getCurrentNode(path);
            if (grp == null) {
                throw new ProvisioningException("Not a valid feature " + feature);
            }
            FeatureInfo fi = grp.getFeature();
            if (fi == null) {
                throw new ProvisioningException("Not a valid feature " + feature);
            }

            session.removeFeature(invoc.getPmSession(), ci, fi);
        } catch (Exception ex) {
            throw new CommandExecutionException(invoc.getPmSession(), CliErrors.removeFeatureFailed(), ex);
        }
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
