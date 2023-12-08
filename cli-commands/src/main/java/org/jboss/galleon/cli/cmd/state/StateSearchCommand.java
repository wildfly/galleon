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
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandDefinition;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Option;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.pkg.AbstractPackageCommand.PackageCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
@CommandDefinition(name = "search", description = HelpDescriptions.SEARCH_STATE)
public class StateSearchCommand extends AbstractStateCommand {

    public static class QueryActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck("package");
            return opt == null || opt.value() == null;
        }
    }

    public static class PackageActivator implements OptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck("query");
            return opt == null || opt.value() == null;
        }
    }

    @Option(required = false, activator = QueryActivator.class, description = HelpDescriptions.SEARCH_QUERY)
    private String query;

    @Option(required = false, name = "package", completer = PackageCompleter.class,
            activator = PackageActivator.class, description = HelpDescriptions.PACKAGE_PATH)
    private String pkg;

    @Option(required = false, name = "include-dependencies", hasValue = false,
            description = HelpDescriptions.SEARCH_IN_DEPENDENCIES)
    private Boolean inDependencies;

    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return "org.jboss.galleon.cli.cmd.state.core.CoreStateSearchCommand";
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the pkg
     */
    public String getPkg() {
        return pkg;
    }

    /**
     * @return the inDependencies
     */
    public Boolean getInDependencies() {
        return inDependencies;
    }
}
