/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
            ParsedOption opt = parsedCommand.findLongOptionNoActivatorCheck(FILE_OPTION_NAME);
            if (opt != null && opt.value() != null) {
                return false;
            }
            return true;
        }
    }
    @Option(name = FILE_OPTION_NAME, required = false, activator = FileActivator.class,
            description = HelpDescriptions.FP_FILE_PATH)
    private File file;

    @Argument(completer = FPLocationCompleter.class, activator = FeaturePackLocationActivator.class,
            required = false, description = HelpDescriptions.FP_LOCATION)
    private String fpl;

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the fpl
     */
    public String getFpl() {
        return fpl;
    }
}
