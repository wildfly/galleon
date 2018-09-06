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
package org.jboss.galleon.cli.cmd.maingrp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.terminal.Key;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.cmd.InstalledProducerCompleter;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.cli.cmd.plugin.AbstractProvisionWithPlugins;
import static org.jboss.galleon.cli.cmd.maingrp.AbstractProvisioningCommand.DIR_OPTION_NAME;
import org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.Updates;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.ALL_DEPENDENCIES_OPTION_NAME;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.UPDATES_AVAILABLE;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.UP_TO_DATE;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.plugin.PluginOption;
import static org.jboss.galleon.cli.cmd.maingrp.CheckUpdatesCommand.FP_OPTION_NAME;

/**
 *
 * @author jdenise@redhat.com
 */
public class UpdateCommand extends AbstractProvisionWithPlugins {

    static final String YES_OPTION_NAME = "yes";


    public UpdateCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        ProcessedOption includeAll = ProcessedOptionBuilder.builder().name(ALL_DEPENDENCIES_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                optionType(OptionType.BOOLEAN).
                description(HelpDescriptions.UPDATE_DEPENDENCIES).
                completer(FileOptionCompleter.class).
                required(false).
                build();
        options.add(includeAll);
        ProcessedOption yes = ProcessedOptionBuilder.builder().name(YES_OPTION_NAME).
                hasValue(false).
                type(Boolean.class).
                optionType(OptionType.BOOLEAN).
                description(HelpDescriptions.UPDATE_NO_CONFIRMATION).
                completer(FileOptionCompleter.class).
                required(false).
                shortName('y').
                build();
        options.add(yes);
        ProcessedOption fp = ProcessedOptionBuilder.builder().name(FP_OPTION_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description(HelpDescriptions.UPDATE_FP).
                required(false).
                completer(InstalledProducerCompleter.class).
                build();
        options.add(fp);
        return options;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
        // No option to validate.
    }

    @Override
    protected List<DynamicOption> getDynamicOptions(State state) throws Exception {
        String targetDirArg = (String) getValue(DIR_OPTION_NAME);
        if (targetDirArg == null) {
            // Check in argument or option, that is the option completion case.
            targetDirArg = getOptionValue(DIR_OPTION_NAME);
        }
        Path workDir = PmSession.getWorkDir(pmSession.getAeshContext());
        Path installation = targetDirArg == null ? workDir : workDir.resolve(targetDirArg);
        ProvisioningConfig config = pmSession.newProvisioningManager(installation, false).getProvisioningConfig();
        Set<PluginOption> opts = pmSession.getResolver().get(null, PluginResolver.newResolver(pmSession, config)).getDiff();
        List<DynamicOption> options = new ArrayList<>();
        for (PluginOption opt : opts) {
            DynamicOption dynOption = new DynamicOption(opt.getName(), opt.isRequired(), opt.isAcceptsValue());
            options.add(dynOption);
        }
        return options;
    }

    @Override
    protected void doRunCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        try {
            ProvisioningManager mgr = getManager(session);
            String fp = getFP();
            Updates updates = CheckUpdatesCommand.getUpdatesTable(mgr, session, allDependencies(), getFP());
            if (updates.plan.isEmpty()) {
                session.println(UP_TO_DATE);
            } else {
                session.println(UPDATES_AVAILABLE);
                session.println(updates.t.build());
                if (!noConfirm()) {
                    try {
                        Key k = null;
                        while (k == null || (!Key.y.equals(k) && !Key.n.equals(k))) {
                            session.print("Proceed with latest updates [y/n]?");
                            KeyAction a = session.input();
                            k = Key.findStartKey(a.buffer().array());
                        }
                        if (Key.n.equals(k)) {
                            return;
                        }
                    } finally {
                        session.println("");
                    }
                }
                mgr.apply(updates.plan, options);
            }
        } catch (ProvisioningException ex) {
            throw new CommandExecutionException(session.getPmSession(),
                    CliErrors.updateFailed(), ex);
        } catch (InterruptedException ignored) {
            // Just exit the command smoothly
        }
    }

    private boolean noConfirm() {
        return contains(YES_OPTION_NAME);
    }

    private String getFP() {
        return (String) getValue(FP_OPTION_NAME);
    }

    private boolean allDependencies() {
        return contains(ALL_DEPENDENCIES_OPTION_NAME);
    }

    @Override
    protected String getName() {
        return "update";
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.UPDATE;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.PROVISIONING;
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }
}
