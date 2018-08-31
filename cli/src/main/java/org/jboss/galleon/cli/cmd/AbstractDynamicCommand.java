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
package org.jboss.galleon.cli.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.container.CommandContainer;
import org.aesh.command.impl.container.AeshCommandContainer;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.impl.parser.AeshCommandLineParser;
import org.aesh.command.map.MapCommand;
import org.aesh.command.map.MapProcessedCommandBuilder;
import org.aesh.command.map.MapProcessedCommandBuilder.MapProcessedCommand;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.parser.OptionParserException;
import org.aesh.parser.ParsedLine;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.model.state.State;

/**
 * Dynamic command support.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractDynamicCommand extends MapCommand<PmCommandInvocation> {

    public static final String ARGUMENT_NAME = "";

    public class DynamicOption {

        private final String name;
        private final boolean hasValue;
        private final boolean required;
        private String defaultValue;

        public DynamicOption(String name, boolean required, boolean hasValue) {
            this.name = name;
            this.required = required;
            this.hasValue = hasValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the hasValue
         */
        public boolean hasValue() {
            return hasValue;
        }

        /**
         * @return the required
         */
        public boolean isRequired() {
            return required;
        }
    }

    private final Map<String, String> renamedOptions = new HashMap<>();

    private class DynamicOptionsProvider implements MapProcessedCommandBuilder.ProcessedOptionProvider {

        @Override
        public List<ProcessedOption> getOptions(List<ProcessedOption> currentOptions) {
            if (!canComplete(pmSession)) {
                return Collections.emptyList();
            }
            try {
                List<ProcessedOption> options = new ArrayList<>();
                List<DynamicOption> parameters = getDynamicOptions(pmSession.getState());
                for (DynamicOption opt : parameters) {
                    // There is no caching, if current options already contains it, do not add it.
                    if (currentOptions != null) {
                        ProcessedOption found = null;
                        for (ProcessedOption option : currentOptions) {
                            if (option.name().equals(opt.getName())) {
                                found = option;
                                break;
                            }
                        }
                        if (found != null) {
                            options.add(found);
                            continue;
                        }
                    }
                    ProcessedOptionBuilder builder = ProcessedOptionBuilder.builder();
                    if (staticOptions.contains(opt.getName())) {
                        renamedOptions.put(rename(opt.getName(), parameters), opt.getName());
                    }
                    builder.name(opt.getName());
                    builder.type(String.class);
                    if (!opt.hasValue()) {
                        noValuesOptions.add(opt.getName());
                    }
                    builder.optionType(opt.hasValue() ? OptionType.NORMAL : OptionType.BOOLEAN);
                    builder.hasValue(opt.hasValue());
                    builder.required(opt.isRequired());
                    if (opt.getDefaultValue() != null) {
                        builder.addDefaultValue(opt.getDefaultValue());
                    }
                    options.add(builder.build());
                }
                return options;
            } catch (Exception ex) {
                Logger.getLogger(AbstractDynamicCommand.class.getName()).log(Level.FINEST,
                        "Error retrieving dynamic options: {0}", ex.getLocalizedMessage());
            }
            return Collections.emptyList();
        }
    }

    protected final PmSession pmSession;
    private final Set<String> staticOptions = new HashSet<>();
    private final Set<String> noValuesOptions = new HashSet<>();
    private MapProcessedCommand cmd;
    private final boolean onlyAtCompletion;
    private final boolean checkForRequired;
    private final boolean optimizeRetrieval;
    /**
     *
     * @param pmSession The session
     * @param optimizeRetrieval True, optimize retrieval.
     */
    public AbstractDynamicCommand(PmSession pmSession, boolean optimizeRetrieval) {
        this.pmSession = pmSession;
        this.onlyAtCompletion = optimizeRetrieval;
        this.checkForRequired = !optimizeRetrieval;
        this.optimizeRetrieval = optimizeRetrieval;
    }

    protected abstract String getName();
    protected abstract String getDescription();

    protected abstract List<DynamicOption> getDynamicOptions(State state) throws Exception;
    protected abstract void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException;
    protected abstract List<ProcessedOption> getStaticOptions() throws OptionParserException;

    protected abstract PmCommandActivator getActivator();

    public CommandContainer<Command<PmCommandInvocation>, PmCommandInvocation> createCommand() throws CommandLineParserException {
        cmd = buildCommand();
        AeshCommandContainer container = new AeshCommandContainer(
                new AeshCommandLineParser<>(cmd));
        return container;
    }

    @Override
    public final boolean checkForRequiredOptions(ParsedLine pl) {
        return checkForRequired;
    }

    @Override
    public CommandResult execute(PmCommandInvocation session) throws CommandException {
        try {
            session.getPmSession().commandStart(session);
            validateOptions(session);
            Map<String, String> options = getOptions();
            runCommand(session, options);
            return CommandResult.SUCCESS;
        } catch (Throwable t) {
            PmSessionCommand.handleException(session, t);
            return CommandResult.FAILURE;
        } finally {
            session.getPmSession().commandEnd(session);
        }
    }

    protected String getArgumentValue() {
        return cmd.getArgument().getValue();
    }

    protected String getOptionValue(String name) {
        for (ProcessedOption opt : cmd.getOptions(false)) {
            if (opt.name().equals(name)) {
                return opt.getValue();
            }
        }
        return null;
    }

    protected List<String> getArgumentsValues() {
        return cmd.getArguments().getValues();
    }

    private String rename(String name, List<DynamicOption> options) {
        // XXX JF DENISE TODO!
        throw new RuntimeException("TODO Must rename " + name);
        //return name;
    }

    private MapProcessedCommand buildCommand() throws CommandLineParserException {
        MapProcessedCommandBuilder builder = new MapProcessedCommandBuilder();
        builder.command(this);
        builder.lookupAtCompletionOnly(onlyAtCompletion);
        builder.name(getName());
        builder.activator(getActivator());

        List<ProcessedOption> otherOptions = getStaticOptions();
        for (ProcessedOption o : otherOptions) {
            staticOptions.add(o.name());
            if (o.name().equals(ARGUMENT_NAME)) {
                if (o.hasMultipleValues()) {
                    builder.arguments(o);
                } else {
                    builder.argument(o);
                }
            } else {
                builder.addOption(o);
            }
        }

        builder.description(getDescription());
        builder.optionProvider(new DynamicOptionsProvider());
        return builder.create();
    }

    private void validateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // Check validity of provided options
        Set<String> providedOptions = getValues().keySet();
        List<ProcessedOption> sOptions = cmd.getOptions(false);

        if (optimizeRetrieval) {
            // some checks have been by-passed for static options.
             // check values
            for (String o : providedOptions) {
                for (ProcessedOption opt : sOptions) {
                    if (opt.name().equals(o)) {
                        String val = (String) getValue(opt.name());
                        if (opt.hasValue() && (val == null || val.isEmpty())) {
                            throw new CommandExecutionException("Option --" + opt.name()
                                    + " was specified, but no value was given");
                        }
                    }
                }
            }
            // check required
            for (ProcessedOption opt : sOptions) {
                if (opt.isRequired() && !providedOptions.contains(opt.name())) {
                    throw new CommandExecutionException("Option --" + opt.name()
                            + " is required for this command.");
                }
            }
        } else {
            List<ProcessedOption> dOptions = cmd.getOptions(true);
            for (String o : providedOptions) {
                boolean found = false;
                if (!ARGUMENT_NAME.equals(o)) {
                    // first find in static options
                    for (ProcessedOption opt : sOptions) {
                        if (opt.name().equals(o)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // then in dynamic ones
                        for (ProcessedOption opt : dOptions) {
                            if (opt.name().equals(o)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            throw new CommandExecutionException("Unknown option --" + o);
                        }
                    }
                }
            }
        }
        doValidateOptions(invoc);
    }

    protected abstract void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException;

    protected abstract boolean canComplete(PmSession pmSession);

    private Map<String, String> getOptions() throws CommandException {
        Map<String, String> options = new HashMap<>();
        for (String m : getValues().keySet()) {
            if (m == null) {
                throw new CommandException("Invalid null option");
            }
            if (!staticOptions.contains(m)) {
                if (noValuesOptions.contains(m)) {
                    options.put(m, null);
                } else {
                    options.put(m, (String) getValue(m));
                }
            }
        }
        return options;
    }
}
