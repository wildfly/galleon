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
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.config.Configuration;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.ResourceResolver;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.Producer;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class PmSession implements CommandInvocationProvider<PmCommandInvocation>, CompleterInvocationProvider<PmCompleterInvocation>,
        CommandActivatorProvider, OptionActivatorProvider<OptionActivator> {

    private class MavenListener implements RepositoryListener {

        private static final String MAVEN = "[MAVEN] ";

        private boolean active;

        void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void artifactDownloaded(RepositoryEvent re) {
            if (active && re != null) {
                String artifact = re.getArtifact().getGroupId() + ":"
                        + re.getArtifact().getArtifactId() + ":"
                        + re.getArtifact().getVersion() + ":"
                        + re.getArtifact().getExtension();
                if (re.getException() == null) {
                    println(MAVEN + "downloaded " + artifact
                            + " from " + re.getRepository().getId());
                } else if (re.getException() instanceof ArtifactNotFoundException) {
                    println(MAVEN + "artifact " + artifact + " not found in " + re.getRepository().getId());
                } else {
                    println(MAVEN + re.getException().getLocalizedMessage() + " while downloading artifact " + artifact);
                }
            }
        }

        @Override
        public void artifactDownloading(RepositoryEvent re) {
            if (active && re != null) {
                println(MAVEN + "attempting to download " + re.getArtifact().getGroupId() + ":"
                        + re.getArtifact().getArtifactId() + ":"
                        + re.getArtifact().getVersion() + ":"
                        + re.getArtifact().getExtension()
                        + (re.getRepository() != null ? " from " + re.getRepository().getId() : ""));
            }
        }

        @Override
        public void artifactDescriptorInvalid(RepositoryEvent re) {
            //session.println("artifactDescriptorInvalid " + re);
        }

        @Override
        public void artifactDescriptorMissing(RepositoryEvent re) {
            //session.println("artifactDescriptorMissing " + re);
        }

        @Override
        public void metadataInvalid(RepositoryEvent re) {
            //session.println("metadataInvalid " + re);
        }

        @Override
        public void artifactResolving(RepositoryEvent re) {
            //session.println("artifactResolving " + re);
        }

        @Override
        public void artifactResolved(RepositoryEvent re) {
            //session.println("artifactResolved " + re);
        }

        @Override
        public void metadataResolving(RepositoryEvent re) {
            //session.println("metadataResolving " + re);

        }

        @Override
        public void metadataResolved(RepositoryEvent re) {
            //session.println("metadataResolved " + re);

        }

        @Override
        public void metadataDownloading(RepositoryEvent re) {
            //session.println("metadataDownloading " + re);
        }

        @Override
        public void metadataDownloaded(RepositoryEvent re) {
            //session.println("metadataDownloaded " + re);
        }

        @Override
        public void artifactInstalling(RepositoryEvent re) {
            //session.println("artifactInstalling " + re);
        }

        @Override
        public void artifactInstalled(RepositoryEvent re) {
            //session.println("artifactInstalled " + re);
        }

        @Override
        public void metadataInstalling(RepositoryEvent re) {
            //session.println("metadataInstalling " + re);
        }

        @Override
        public void metadataInstalled(RepositoryEvent re) {
            //session.println("metadataInstalled " + re);
        }

        @Override
        public void artifactDeploying(RepositoryEvent re) {
            //session.println("artifactDeploying " + re);
        }

        @Override
        public void artifactDeployed(RepositoryEvent re) {
            //session.println("artifactDeployed " + re);
        }

        @Override
        public void metadataDeploying(RepositoryEvent re) {
            //session.println("metadataDeploying " + re);
        }

        @Override
        public void metadataDeployed(RepositoryEvent re) {
            //session.println("metadataDeployed " + re);
        }
    }
    private PrintStream out;
    private PrintStream err;
    private final Configuration config;
    private State state;
    private FeatureContainer exploredContainer;
    private String currentPath;
    private final CliMavenArtifactRepositoryManager maven;
    private final MavenListener mavenListener;
    private UniverseManager universe;
    private final ResourceResolver resolver;
    private final ProvisioningLayoutFactory layoutFactory;
    private AeshContext ctx;
    public PmSession(Configuration config) throws Exception {
        this.config = config;
        this.mavenListener = new MavenListener();
        this.maven = new CliMavenArtifactRepositoryManager(config.getMavenConfig(),
                mavenListener);
        universe = new UniverseManager(this, config, maven);
        layoutFactory = ProvisioningLayoutFactory.getInstance(universe.getUniverseResolver());
        resolver = new ResourceResolver(this);
        // Abort running universe resolution if any.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (state != null) {
                    state.close();
                }
            } finally {
                try {
                    universe.close();
                } finally {
                    layoutFactory.close();
                }
            }
        }));
    }

    void setAeshContext(AeshContext ctx) {
        this.ctx = ctx;
    }

    public AeshContext getAeshContext() {
        return ctx;
    }

    public ProvisioningManager newProvisioningManager(Path installation, boolean verbose) throws ProvisioningException {
        ProvisioningManager.Builder builder = ProvisioningManager.builder();
        builder.setLayoutFactory(getLayoutFactory());
        if (installation != null) {
            builder.setInstallationHome(installation);
        }
        builder.setMessageWriter(new DefaultMessageWriter(out,
                err, verbose));
        return builder.build();
    }

    public ProvisioningLayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public ResourceResolver getResolver() {
        return resolver;
    }

    public UniverseManager getUniverse() {
        return universe;
    }

    public FeaturePackLocation getResolvedLocation(String location) throws ProvisioningException {
        if (location.endsWith("" + FeaturePackLocation.FREQUENCY_START)
                || location.endsWith("" + FeaturePackLocation.BUILD_START)) {
            location = location.substring(0, location.length() - 1);
        }
        FeaturePackLocation loc = FeaturePackLocation.fromString(location);
        return getResolvedLocation(loc);
    }


    public void commandStart() {
        maven.commandStart();
    }

    public void commandEnd() {
        maven.commandEnd();
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

    public void print(String txt) {
        out.print(txt);
    }

    public Configuration getPmConfiguration() {
        return config;
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

    public boolean existsInLocalRepository(FPID fpid) throws ProvisioningException {
        FeaturePackLocation loc = getResolvedLocation(fpid.getLocation());
        Producer<?> producer = universe.getUniverseResolver().getUniverse(loc.
                getUniverse()).getProducer(loc.getProducerName());
        boolean hasFrequency = false;
        if (producer != null) {
            hasFrequency = producer.hasFrequencies();
        }
        String freq = loc.getFrequency() == null && hasFrequency ? "alpha" : loc.getFrequency();
        loc = new FeaturePackLocation(loc.getUniverse(),
                loc.getProducerName(), loc.getChannelName(), freq, loc.getBuild());
        return universe.getUniverseResolver().isResolved(loc);
    }

    public void downloadFp(FPID fpid) throws ProvisioningException {
        universe.getUniverseResolver().resolve(getResolvedLocation(fpid.getLocation()));
    }

    private FeaturePackLocation getResolvedLocation(FeaturePackLocation fplocation) throws ProvisioningException {
        UniverseSpec spec = fplocation.getUniverse();
        if (spec != null) {
            if (spec.getLocation() == null) {
                spec = universe.getUniverseSpec(spec.getFactory());
                if (spec == null) {
                    throw new ProvisioningException("Unknown universe for " + fplocation);
                }
            }
        } else {
            spec = universe.getDefaultUniverseSpec();
        }
        return new FeaturePackLocation(spec, fplocation.getProducerName(),
                fplocation.getChannelName(), fplocation.getFrequency(), fplocation.getBuild());
    }

    public FeaturePackLocation getExposedLocation(FeaturePackLocation fplocation) {
        // Expose the default or name.
        UniverseSpec spec = fplocation.getUniverse();
        boolean rewrite = false;
        String name = getUniverse().getUniverseName(spec);
        if (name != null) {
            rewrite = true;
            spec = new UniverseSpec(name, null);
        } else if (getUniverse().getDefaultUniverseSpec().equals(spec)) {
            rewrite = true;
            spec = null;
        }
        if (rewrite) {
            fplocation = new FeaturePackLocation(spec,
                    fplocation.getProducerName(), fplocation.getChannelName(),
                    fplocation.getFrequency(), fplocation.getBuild());
        }
        return fplocation;
    }
    public void enableMavenTrace(boolean b) {
        mavenListener.setActive(b);
    }
}
