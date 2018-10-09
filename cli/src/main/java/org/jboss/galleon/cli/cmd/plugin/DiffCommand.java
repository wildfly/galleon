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
package org.jboss.galleon.cli.cmd.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.resolver.PluginResolver;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise@redhat.com
 */
// Can be removed/refactored when we know what to do with it.
@Deprecated
public class DiffCommand extends AbstractPluginsCommand {

    private static final String SRC_NAME = "src";
    private static final String TARGET_NAME = "target";
    public DiffCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options, FeaturePackLocation loc) throws CommandExecutionException {
        /*
        try {
            Path targetDirectory = toPath((String) getValue(TARGET_NAME), session.getAeshContext());
            getManager(session).exportConfigurationChanges(targetDirectory, loc == null ? null : loc.getFPID(), options);
        } catch (ProvisioningException | IOException ex) {
            ex.printStackTrace();
            throw new CommandExecutionException(session.getPmSession(), CliErrors.diffFailed(), ex);
        }
        */
    }

    @Override
    protected Set<ProvisioningOption> getPluginOptions(FeaturePackLocation loc) throws ProvisioningException {
        try {
            return pmSession.getResolver().get(loc.toString(),
                    PluginResolver.newResolver(pmSession, loc)).getDiff();
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new ProvisioningException(ex);
        } catch (ExecutionException ex) {
            throw new ProvisioningException(ex.getCause());
        }
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        ProcessedOption srcDir = ProcessedOptionBuilder.builder().name(SRC_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description(HelpDescriptions.DIFF_SRC_DIR).
                completer(FileOptionCompleter.class).
                build();
        options.add(srcDir);
        ProcessedOption targetDir = ProcessedOptionBuilder.builder().name(TARGET_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description(HelpDescriptions.DIFF_TARGET_DIR).
                completer(FileOptionCompleter.class).
                build();
        options.add(targetDir);
        return options;
    }

    @Override
    protected void doValidateOptions(PmCommandInvocation invoc) throws CommandExecutionException {
    }

    @Override
    protected String getName() {
        return "diff";
    }

    @Override
    protected String getDescription() {
        return HelpDescriptions.DIFF;
    }

    @Override
    public Path getInstallationDirectory(AeshContext context) {
        final String srcPath = (String) getValue(SRC_NAME);
        return srcPath == null ? PmSession.getWorkDir(context) : toPath(srcPath, context);
    }

    private Path toPath(String value, AeshContext context) {
        Path workDir = PmSession.getWorkDir(context);
        return value == null ? PmSession.getWorkDir(context) : workDir.resolve(value);
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }

    @Override
    protected boolean canComplete(PmSession pmSession) {
        Path installation = getPathOption(SRC_NAME);
        String target = null;
        if (Files.exists(PathsUtils.getProvisioningXml(installation))) {
            target = (String) getValue(TARGET_NAME);
            if (target == null) {
                // Check in argument or option, that is the option completion case.
                target = getOptionValue(TARGET_NAME);
            }
        }
        return target != null;
    }

    private Path getPathOption(String name) {
        String path = (String) getValue(name);
        if (path == null) {
            // Check in argument or option, that is the option completion case.
            path = getOptionValue(name);
        }
        Path workDir = PmSession.getWorkDir(pmSession.getAeshContext());
        return path == null ? workDir : workDir.resolve(path);
    }

    @Override
    public CommandDomain getDomain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
