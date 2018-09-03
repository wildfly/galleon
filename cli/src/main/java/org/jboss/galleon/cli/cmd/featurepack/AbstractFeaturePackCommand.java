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
package org.jboss.galleon.cli.cmd.featurepack;

import java.io.File;
import org.aesh.command.impl.internal.ParsedCommand;
import org.aesh.command.impl.internal.ParsedOption;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmOptionActivator;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.FPLocationCompleter;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractFeaturePackCommand extends PmSessionCommand {

    private static final String FILE_OPTION_NAME = "file";
    public static class FileActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            String argumentValue = parsedCommand.argument().value();
            if (argumentValue != null) {
                return false;
            }
            return true;
        }
    }

    public static class FeaturePackLocationActivator extends PmOptionActivator {

        @Override
        public boolean isActivated(ParsedCommand parsedCommand) {
            if (getPmSession().getContainer() != null) {
                return false;
            }
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(FILE_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            return true;
        }
    }
    @Option(name = FILE_OPTION_NAME, required = false, activator = FileActivator.class,
            description = HelpDescriptions.FP_FILE_PATH)
    protected File file;

    @Argument(completer = FPLocationCompleter.class, activator = FeaturePackLocationActivator.class,
            required = false, description = HelpDescriptions.FP_LOCATION)
    protected String fpl;
}
