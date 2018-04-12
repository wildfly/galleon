/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.completer.FileOptionCompleter;
import org.aesh.command.impl.internal.OptionType;
import org.aesh.command.impl.internal.ProcessedOption;
import org.aesh.command.impl.internal.ProcessedOptionBuilder;
import org.aesh.command.parser.OptionParserException;
import org.aesh.readline.AeshContext;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.NoStateCommandActivator;
import org.jboss.galleon.plugin.InstallPlugin;
import org.jboss.galleon.plugin.PluginOption;
import org.jboss.galleon.runtime.ProvisioningRuntime;

/**
 *
 * @author jdenise@redhat.com
 */
public class InstallCommand extends AbstractPluginsCommand {

    private static final String DIR_NAME = "dir";

    public InstallCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected void runCommand(PmCommandInvocation session, Map<String, String> options) throws CommandExecutionException {
        try {
            final ProvisioningManager manager = getManager(session);
            manager.install(getGav(session.getPmSession()), options);
        } catch (Exception ex) {
            throw new CommandExecutionException(ex);
        }
    }

    @Override
    protected Set<PluginOption> getPluginOptions(ProvisioningRuntime runtime) throws ProvisioningException {
        Set<PluginOption> pluginOptions = new HashSet<>();
        ProvisioningRuntime.PluginVisitor<InstallPlugin> visitor = new ProvisioningRuntime.PluginVisitor<InstallPlugin>() {
            @Override
            public void visitPlugin(InstallPlugin plugin) throws ProvisioningException {
                pluginOptions.addAll(plugin.getOptions().values());
            }
        };
        runtime.visitePlugins(visitor, InstallPlugin.class);
        return pluginOptions;
    }

    @Override
    protected String getName() {
        return "install";
    }

    @Override
    protected String getDescription() {
        return "Installs specified feature-pack";
    }

    @Override
    protected List<ProcessedOption> getOtherOptions() throws OptionParserException {
        List<ProcessedOption> options = new ArrayList<>();
        ProcessedOption dir = ProcessedOptionBuilder.builder().name(DIR_NAME).
                hasValue(true).
                type(String.class).
                optionType(OptionType.NORMAL).
                description("Target installation directory.").
                completer(FileOptionCompleter.class).
                build();
        options.add(dir);
        return options;
    }

    @Override
    protected Path getInstallationHome(AeshContext context) {
        String targetDirArg = (String) getValue(DIR_NAME);
        Path workDir = PmSession.getWorkDir(context);
        return targetDirArg == null ? PmSession.getWorkDir(context) : workDir.resolve(targetDirArg);
    }

    @Override
    protected PmCommandActivator getActivator() {
        return new NoStateCommandActivator();
    }
}
