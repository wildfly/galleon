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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import org.aesh.command.activator.CommandActivator;
import org.aesh.command.activator.CommandActivatorProvider;
import org.aesh.command.activator.OptionActivator;
import org.aesh.command.activator.OptionActivatorProvider;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.completer.CompleterInvocationProvider;
import org.aesh.io.FileResource;
import org.aesh.io.Resource;
import org.aesh.readline.AeshContext;
import org.aesh.readline.Prompt;
import org.aesh.utils.Config;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.config.Configuration;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.ResourceResolver;
import org.jboss.galleon.cli.tracking.ProgressTrackers;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.cache.FeaturePackCacheManager;
import org.jboss.galleon.cache.FeaturePackCacheManager.OverwritePolicy;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.jboss.galleon.util.IoUtils;
import org.jboss.galleon.util.LayoutUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class PmSession implements CompleterInvocationProvider<PmCompleterInvocation>,
        CommandActivatorProvider, OptionActivatorProvider<OptionActivator> {

    private class CliOverwritePolicy implements OverwritePolicy {

        // The same FPID can be seen during the execution of a single command.
        // Re-use it if already seen (and un-packed).
        // This set is also used to record what has been cached.
        private final Set<FPID> seen = new HashSet<>();

        void commandStart() {
            seen.clear();
        }

        void commandEnd() {
            if (seen.isEmpty()) {
                return;
            }
            try {
                Properties props = config.getLayoutCacheContent();
                long time = System.currentTimeMillis();
                for (FPID id : seen) {
                    props.setProperty(id.toString(), "" + time);
                }
                config.storeLayoutCacheContent(props);
            } catch (IOException ex) {
                CliLogging.exception(ex);
            }
        }

        @Override
        public boolean hasExpired(Path fpDir, FeaturePackLocation.FPID fpid) {
            try {
                boolean devBuild = universe.getUniverse(fpid.getUniverse()).
                        getProducer(fpid.getProducer().getName()).getChannel(fpid.getChannel().getName()).
                        isDevBuild(fpid);
                return devBuild && !seen.contains(fpid);
            } catch (ProvisioningException ex) {
                CliLogging.exception(ex);
                return true;
            }
        }

        @Override
        public void cached(FPID fpid) {
            seen.add(fpid);
        }
    }

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
//            if (active && re != null) {
//                println("artifactResolving " + re);
//            }
        }

        @Override
        public void artifactResolved(RepositoryEvent re) {
//            if (active && re != null) {
//                println("artifactResolved " + re);
//            }
        }

        @Override
        public void metadataResolving(RepositoryEvent re) {
//            if (active && re != null) {
//                println("metadataResolving " + re);
//            }
        }

        @Override
        public void metadataResolved(RepositoryEvent re) {
//            if (active && re != null) {
//                println("metadataResolved " + re);
//            }
        }

        @Override
        public void metadataDownloading(RepositoryEvent re) {
//            if (active && re != null) {
//                println("metadataDownloading " + re);
//            }
        }

        @Override
        public void metadataDownloaded(RepositoryEvent re) {
//            if (active && re != null) {
//                println("metadataDownloaded " + re);
//            }
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
    private static final String EDIT_MODE_PROMPT = "!edit!";
    private PrintStream out;
    private PrintStream err;
    private final Configuration config;
    private State state;
    private String currentPath;
    private final CliMavenArtifactRepositoryManager maven;
    private final MavenListener mavenListener;
    private final UniverseManager universe;
    private final ResourceResolver resolver;
    private final ProvisioningLayoutFactory layoutFactory;
    private AeshContext ctx;
    private boolean rethrow = false;
    private boolean enableTrackers = true;
    private final CliOverwritePolicy policy = new CliOverwritePolicy();
    private final boolean interactive;
    private final FeaturePackCacheManager cacheManager;
    private ToolModes toolModes;
    private String promptRoot;
    private Path previousDir;

    public PmSession(Configuration config) throws Exception {
        this(config, true);
    }

    public PmSession(Configuration config, boolean interactive) throws Exception {
        this.config = config;
        this.mavenListener = new MavenListener();
        this.maven = new CliMavenArtifactRepositoryManager(config.getMavenConfig(),
                mavenListener);
        UniverseResolver universeResolver = UniverseResolver.builder().addArtifactResolver(maven).build();
        universe = new UniverseManager(this, config, maven, universeResolver);
        this.interactive = interactive;
        cacheManager = new FeaturePackCacheManager(config.getLayoutCache(), policy);
        if (interactive) {
            layoutFactory = ProvisioningLayoutFactory.getInstance(universeResolver, cacheManager);
        } else {
            layoutFactory = ProvisioningLayoutFactory.getInstance(universeResolver);
        }
        resolver = new ResourceResolver(this);
        // Abort running universe resolution if any.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
    }

    void setModes(ToolModes toolModes) {
        this.toolModes = toolModes;
    }

    ToolModes getToolModes() {
        return toolModes;
    }

    public void clearLayoutCache() throws IOException {
        config.clearLayoutCache();
    }

    public void cleanupLayoutCache() {
        try {
            if (!Files.exists(cacheManager.getHome())) {
                return;
            }
            Properties props = config.getLayoutCacheContent();
            long now = System.currentTimeMillis();
            Set<String> toRemove = new HashSet<>();
            Set<Path> fpPaths = new HashSet<>();
            for (String k : props.stringPropertyNames()) {
                String time = props.getProperty(k);
                long lastUsage = Long.decode(time);
                Path p = LayoutUtils.getFeaturePackDir(config.getLayoutCache(),
                        FeaturePackLocation.fromString(k).getFPID());
                if (!Files.exists(p)) {
                    toRemove.add(k);
                } else if ((now - lastUsage) > Configuration.CACHE_PERIOD) {
                    toRemove.add(k);
                    try {
                        cacheManager.remove(FeaturePackLocation.fromString(k).getFPID());
                    } catch (ProvisioningException ex) {
                        CliLogging.exception(ex);
                    }
                }
                // keep root dir to check that the dirs located in cache are valid
                // feature packs paths. On JDK8 and earlier due to JDK-8013099
                // we can have layout dir that can't be deleted.
                Iterator<Path> it = cacheManager.getHome().relativize(p).iterator();
                if (it.hasNext()) {
                    fpPaths.add(it.next());
                }
            }
            if (!toRemove.isEmpty()) {
                for (String k : toRemove) {
                    props.remove(k);
                }
                config.storeLayoutCacheContent(props);
            }

            // Cleanup directories that are leftovers from previous executions
            Files.list(cacheManager.getHome()).forEach(new Consumer<Path>() {
                @Override
                public void accept(Path t) {
                    if (!fpPaths.contains(t.getFileName())) {
                        IoUtils.recursiveDelete(t);
                    }
                }
            });
        } catch (ProvisioningDescriptionException | IOException ex) {
            CliLogging.exception(ex);
        }

    }

    public void enableTrackers(boolean enable) {
        enableTrackers = enable;
    }

    public boolean isTrackersEnabled() {
        return enableTrackers;
    }

    public void registerTrackers() {
        if (enableTrackers) {
            ProgressTrackers.registerTrackers(this);
        }
    }

    public void unregisterTrackers() {
        ProgressTrackers.unregisterTrackers(this);
    }

    public void throwException() {
        rethrow = true;
    }

    public boolean isExceptionRethrown() {
        return rethrow;
    }

    public void close() {
        try {
            if (state != null) {
                state.close();
            }
        } finally {
            try {
                universe.close();
            } finally {
                if (interactive) {
                    layoutFactory.checkOpenLayouts();
                } else {
                    layoutFactory.close();
                }
            }
        }
    }

    MavenRepoManager getMavenRepoManager() {
        return maven;
    }

    void setAeshContext(AeshContext ctx) {
        this.ctx = ctx;
    }

    public AeshContext getAeshContext() {
        return ctx;
    }

    // That is the method to call in order to build a ProvisioningManager.
    // It has a side effect on progress tracking.
    // Progress tracking is disabled when verbose is enabled.
    public ProvisioningManager newProvisioningManager(Path installation, boolean verbose) throws ProvisioningException {
        ProvisioningManager.Builder builder = ProvisioningManager.builder();
        builder.setLayoutFactory(getLayoutFactory());
        if (installation != null) {
            builder.setInstallationHome(installation);
        }
        builder.setMessageWriter(getMessageWriter(verbose));
        return builder.build();
    }

    public MessageWriter getMessageWriter(boolean verbose) {
        if (enableTrackers) {
            // In verbose mode, do not mix verbose content with the output from progress tracker.
            if (verbose) {
                unregisterTrackers();
            } else {
                registerTrackers();
            }
        } else {
            unregisterTrackers();
        }
        return new DefaultMessageWriter(out, err, verbose);
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

    public FeaturePackLocation getResolvedLocation(Path installation, String location) throws ProvisioningException {
        if (location.endsWith("" + FeaturePackLocation.FREQUENCY_START)
                || location.endsWith("" + FeaturePackLocation.BUILD_START)) {
            location = location.substring(0, location.length() - 1);
        }
        // A producer spec without any universe nor channel.
        if (!location.contains("" + FeaturePackLocation.UNIVERSE_START) && !location.contains("" + FeaturePackLocation.CHANNEL_START)) {
            location = new FeaturePackLocation(universe.getDefaultUniverseSpec(installation), location, null, null, null).toString();
        }
        FeaturePackLocation loc = FeaturePackLocation.fromString(location);
        return getResolvedLocation(installation, loc);
    }


    public void commandStart(PmCommandInvocation session) {
        if (interactive) {
            policy.commandStart();
        }
        maven.commandStart();
        ProgressTrackers.commandStart(session);
    }

    public void commandEnd(PmCommandInvocation session) {
        if (interactive) {
            policy.commandEnd();
        }
        maven.commandEnd();
        ProgressTrackers.commandEnd(session);
    }

    public void setState(State state) {
        if (state == null) {
            if (this.state != null) {
                this.state.close();
            }
            setCurrentPath(null);
            // back to nominal
            toolModes.setMode(ToolModes.Mode.NOMINAL);
            promptRoot = null;
        } else {
            toolModes.setMode(ToolModes.Mode.EDIT);
            promptRoot = EDIT_MODE_PROMPT + (state.getName() == null ? ""
                    : state.getName() + "!");
        }
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public FeatureContainer getContainer() {
        if (state != null) {
            return state.getContainer();
        }
        return null;
    }

    public String getCurrentPath() {
        if (state != null) {
            return state.getPath();
        }
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        if (state != null) {
            state.setPath(currentPath);
        }
        this.currentPath = currentPath;
    }

    public Path getPreviousDirectory() {
        return previousDir;
    }

    public void setCurrentDirectory(Path path) throws IOException {
        Resource res = new FileResource(path);
        final List<Resource> files = res.resolve(ctx.getCurrentWorkingDirectory());
        if (files.get(0).isDirectory()) {
            previousDir = Paths.get(ctx.getCurrentWorkingDirectory().getAbsolutePath());
            ctx.setCurrentWorkingDirectory(files.get(0));
        } else {
            throw new IOException(CliErrors.unknownDirectory(path.getFileName().toString()));
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

    public Prompt buildPrompt() {
        ToolModes.Mode mode = toolModes.getActiveMode();
        Prompt prompt = null;
        switch (mode) {
            case NOMINAL: {
                prompt = buildPrompt(ctx.getCurrentWorkingDirectory().getName());
                break;
            }
            case EDIT: {
                prompt = buildPrompt("");
                break;
            }
        }
        return prompt;
    }

    public Prompt buildPrompt(String name) {
        return new Prompt(new StringBuilder().append('[')
                .append(promptRoot == null ? "" : promptRoot)
                .append(name)
                .append("]$ ").toString());
    }

    public static Path getWorkDir(AeshContext aeshCtx) {
        return Paths.get(aeshCtx.getCurrentWorkingDirectory().getAbsolutePath());
    }

    void setOut(PrintStream out) {
        this.out = out;
    }

    void setErr(PrintStream err) {
        this.err = err;
    }

    PrintStream getErr() {
        return err;
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

    public void downloadFp(FPID fpid) throws ProvisioningException {
        universe.resolve(getResolvedLocation(null, fpid.getLocation()));
    }

    private FeaturePackLocation getResolvedLocation(Path installation, FeaturePackLocation fplocation) throws ProvisioningException {
        UniverseSpec spec = fplocation.getUniverse();
        if (spec != null) {
            if (spec.getLocation() == null) {
                spec = universe.getUniverseSpec(installation, spec.getFactory());
                if (spec == null) {
                    throw new ProvisioningException("Unknown universe for " + fplocation);
                }
            }
        } else {
            spec = universe.getDefaultUniverseSpec(installation);
        }
        return new FeaturePackLocation(spec, fplocation.getProducerName(),
                fplocation.getChannelName(), fplocation.getFrequency(), fplocation.getBuild());
    }

    public FeaturePackLocation getExposedLocation(Path installation, FeaturePackLocation fplocation) {
        // Expose the default or name.
        UniverseSpec spec = fplocation.getUniverse();
        boolean rewrite = false;
        String name = getUniverse().getUniverseName(installation, spec);
        if (name != null) {
            rewrite = true;
            spec = new UniverseSpec(name, null);
        } else if (getUniverse().getDefaultUniverseSpec(installation).equals(spec)) {
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
