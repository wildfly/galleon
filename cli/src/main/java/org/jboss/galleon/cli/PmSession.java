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
package org.jboss.galleon.cli;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.activator.CommandActivatorProvider;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.activator.OptionActivatorProvider;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.completer.CompleterInvocationProvider;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.invocation.CommandInvocationProvider;
import org.aesh.readline.AeshContext;
import org.aesh.readline.Prompt;
import org.aesh.utils.Config;
import org.jboss.galleon.ArtifactRepositoryManager;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;

/**
 *
 * @author Alexey Loubyansky
 */
public class PmSession implements CommandInvocationProvider<PmCommandInvocation>, CompleterInvocationProvider<PmCompleterInvocation>,
        CommandActivatorProvider, OptionActivatorProvider<OptionActivator> {

    private PrintStream out;
    private PrintStream err;
    private final Configuration config;
    private final Universes universes;

    private State state;
    private FeatureContainer exploredContainer;
    private String currentPath;
    public PmSession(Configuration config) throws Exception {
        this.config = config;
        //Build the universes
        this.universes = Universes.buildUniverses(MavenArtifactRepositoryManager.getInstance(), config.getUniversesLocations());
    }

    public void setState(State session) {
        this.state = session;
    }

    public State getState() {
        return state;
    }

    public void setExploredContainer(FeatureContainer exploredContainer) {
        this.exploredContainer = exploredContainer;
    }

    public FeatureContainer getExploredContainer() {
        return exploredContainer;
    }

    public FeatureContainer getContainer() {
        if (state != null) {
            return state.getContainer();
        }
        if (exploredContainer != null) {
            return exploredContainer;
        }
        return null;
    }

    public String getCurrentPath() {
        if (state != null) {
            return state.getPath();
        }
        if (currentPath != null) {
            return currentPath;
        }
        return null;
    }

    public void setCurrentPath(String currentPath) {
        if (state != null) {
            state.setPath(currentPath);
        }
        if (currentPath != null) {
            this.currentPath = currentPath;
        }
    }

    public void println(String txt) {
        out.print(txt + Config.getLineSeparator());
    }

    public Configuration getPmConfiguration() {
        return config;
    }

    public Universes getUniverses() {
        return universes;
    }

    public ArtifactRepositoryManager getArtifactResolver() {
        return MavenArtifactRepositoryManager.getInstance();
    }

    // TO REMOVE when we have an universe for sure.
    public boolean hasPopulatedUniverse() {
        for (Universe u : universes.getUniverses()) {
            if (!u.getStreamLocations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static Prompt buildPrompt(AeshContext aeshCtx) {
        return buildPrompt(aeshCtx.getCurrentWorkingDirectory().getName());
    }

    public static Prompt buildPrompt(String name) {
        return new Prompt(new StringBuilder().append('[')
                .append(name)
                .append("]$ ").toString());
    }

    public static Path getWorkDir(AeshContext aeshCtx) {
        return Paths.get(aeshCtx.getCurrentWorkingDirectory().getAbsolutePath());
    }

    @Override
    public PmCommandInvocation enhanceCommandInvocation(CommandInvocation commandInvocation) {
        return new PmCommandInvocation(this, out, err, commandInvocation);
    }

    void setOut(PrintStream out) {
        this.out = out;
    }

    void setErr(PrintStream err) {
        this.err = err;
    }

    @Override
    public PmCompleterInvocation enhanceCompleterInvocation(CompleterInvocation completerInvocation) {
        return new PmCompleterInvocation(completerInvocation, this);
    }

    @Override
    public CommandActivator enhanceCommandActivator(CommandActivator ca) {
        if (ca instanceof PmCommandActivator) {
            ((PmCommandActivator) ca).setPmSession(this);
        }
        return ca;
    }

    @Override
    public OptionActivator enhanceOptionActivator(OptionActivator oa) {
        if (oa instanceof PmOptionActivator) {
            ((PmOptionActivator) oa).setPmSession(this);
        }
        return oa;
    }
}
