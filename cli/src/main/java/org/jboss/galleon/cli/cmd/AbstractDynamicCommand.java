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
import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.container.CommandContainer;
import org.aesh.command.impl.container.AeshCommandContainer;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedCommand;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.impl.parser.AeshCommandLineParser;
import org.aesh.command.map.MapCommand;
import org.aesh.command.map.MapProcessedCommandBuilder;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.parser.OptionParserException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.model.state.State;

/**
 * Dynamic command that retrieves options based on the argument value.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractDynamicCommand extends MapCommand<PmCommandInvocation> {

    public static final String ARGUMENT_NAME = "org.jboss.pm.tool.arg";

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

    private final Map<String, List<ProcessedOption>> dynamicOptions = new HashMap<>();
    private final Map<String, String> renamedOptions = new HashMap<>();
    private class DynamicOptionsProvider implements MapProcessedCommandBuilder.ProcessedOptionProvider {

        @Override
        public List<ProcessedOption> getOptions(List<ProcessedOption> currentOptions) {
            try {
                String id = getId(pmSession);
                if (id != null) {
                    // We can retrieve options
                    List<ProcessedOption> options = dynamicOptions.get(id);
                    if (options == null) {
                        options = new ArrayList<>();
                        List<DynamicOption> parameters = getDynamicOptions(pmSession.getState(), id);
                        for (DynamicOption opt : parameters) {
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
                        dynamicOptions.put(id, options);
                    }
                    return options;
                }
            } catch (Exception ex) {
                // XXX OK.
            }
            return Collections.emptyList();
        }

    }

    protected final PmSession pmSession;
    private final Set<String> staticOptions = new HashSet<>();
    private final Set<String> noValuesOptions = new HashSet<>();
    private ProcessedCommand<?> cmd;

    public AbstractDynamicCommand(PmSession pmSession) {
        this.pmSession = pmSession;
    }

    protected abstract String getId(PmSession session);
    protected abstract String getName();
    protected abstract String getDescription();

    protected abstract List<DynamicOption> getDynamicOptions(State state, String id) throws Exception;
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
    public CommandResult execute(PmCommandInvocation session) throws CommandException {
        try {
            validateOptions();
            Map<String, String> options = getOptions();
            runCommand(session, options);
            return CommandResult.SUCCESS;
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                t.printStackTrace(session.getErr());
            }

            session.print("Error: ");
            println(session, t);

            t = t.getCause();
            int offset = 1;
            while (t != null) {
                for (int i = 0; i < offset; ++i) {
                    session.print(" ");
                }
                session.print("* ");
                println(session, t);
                t = t.getCause();
                ++offset;
            }
            return CommandResult.FAILURE;
        }
    }

    protected String getArgumentValue() {
        return cmd.getArgument().getValue();
    }

    protected List<String> getArgumentsValues() {
        return cmd.getArguments().getValues();
    }

    private String rename(String name, List<DynamicOption> options) {
        // XXX JF DENISE TODO!
        throw new RuntimeException("TODO Must rename " + name);
        //return name;
    }

    private ProcessedCommand buildCommand() throws CommandLineParserException {
        MapProcessedCommandBuilder builder = new MapProcessedCommandBuilder();
        builder.command(this);
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

    private void validateOptions() throws CommandException {
        // Check validity of options
        for (String o : getValues().keySet()) {
            boolean found = false;
            if (!ARGUMENT_NAME.equals(o)) {
                for (ProcessedOption opt : cmd.getOptions()) {
                    if (opt.name().equals(o)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new CommandException("Unknown option " + o);
                }
            }
        }
        doValidateOptions();
    }

    protected abstract void doValidateOptions() throws CommandException;

    private void println(PmCommandInvocation session, Throwable t) {
        if (t.getLocalizedMessage() == null) {
            session.println(t.getClass().getName());
        } else {
            session.println(t.getLocalizedMessage());
        }
    }

    private Map<String, String> getOptions() throws CommandException {
        Map<String, String> options = new HashMap<>();
        for (String m : getValues().keySet()) {
            if (m == null || m.isEmpty()) {
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
