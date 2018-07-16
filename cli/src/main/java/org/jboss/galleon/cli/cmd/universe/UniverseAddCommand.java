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
package org.jboss.galleon.cli.cmd.universe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.aesh.command.CommandDefinition;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.universe.UniverseFactoryLoader;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "add", description = "Add a universe, without a name, set the default universe.")
public class UniverseAddCommand extends PmSessionCommand {

    public static class UniverseFactoryCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            List<String> names = new ArrayList<>();
            names.addAll(UniverseFactoryLoader.getInstance().getFactories());
            return names;
        }
    }
    @Option(completer = UniverseFactoryCompleter.class, required = true)
    private String factory;

    @Option(required = false)
    private String name;

    @Option(required = true)
    private String location;

    @Override
    protected void runCommand(PmCommandInvocation commandInvocation) throws CommandExecutionException {
        try {
            commandInvocation.getPmSession().getUniverse().addUniverse(name, factory, location);
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(commandInvocation.getPmSession(), CliErrors.addUniverseFailed(), ex);
        }
    }

}
