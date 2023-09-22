/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.aesh.terminal.Connection;
import org.aesh.utils.Config;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.APIVersion;
import org.jboss.galleon.api.GalleonBuilder;
import org.jboss.galleon.cli.config.Configuration;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class PmSession implements CompleterInvocationProvider<PmCompleterInvocation>,
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

    private final CliMavenArtifactRepositoryManager maven;
    private final MavenListener mavenListener;
    private final UniverseManager universe;
    //private final ResourceResolver resolver;
    private AeshContext ctx;
    private boolean rethrow = false;
    private final boolean interactive;
    private ToolModes toolModes;
    private String promptRoot;
    private Path previousDir;
    private Connection connection;
    private GalleonBuilder galleonBuilder;
    private GalleonCommandExecutionContext state;
    private final UniverseResolver universeResolver;

    public PmSession(Configuration config) throws Exception {
        this(config, true);
    }

    public PmSession(Configuration config, UniverseSpec builtin) throws Exception {
        this(config, true, builtin);
    }

    public PmSession(Configuration config, boolean interactive) throws Exception {
        this(config, interactive, null);
    }

    public PmSession(Configuration config, boolean interactive, UniverseSpec builtin) throws Exception {
        this.config = config;
        this.mavenListener = new MavenListener();
        this.maven = new CliMavenArtifactRepositoryManager(config.getMavenConfig(),
                mavenListener);
        universeResolver = UniverseResolver.builder().addArtifactResolver(maven).build();
        universe = new UniverseManager(this, config, maven, universeResolver, builtin);
        this.interactive = interactive;
        galleonBuilder = new GalleonBuilder().setUniverseResolver(universeResolver);
        //resolver = new ResourceResolver(this);
        // Abort running universe resolution if any.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
    }
    public GalleonCommandExecutionContext getState() {
        return state;
    }

    public void setState(GalleonCommandExecutionContext state) {
        this.state = state;
    }
    public GalleonBuilder getGalleonBuilder() {
        return galleonBuilder;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public UniverseResolver getUniverseResolver() {
        return universeResolver;
    }
    private static final Map<String, GalleonCommandExecutionContext> contexts = new HashMap<>();

    private static synchronized GalleonCommandExecutionContext getCoreContext(String version, PmSession session, GalleonBuilder galleonBuilder) throws Exception {
        GalleonCommandExecutionContext ctx = contexts.get(version);
        if (ctx == null) {
            // Build a new context
            URLClassLoader coreLoader = galleonBuilder.getCoreClassLoader(version);
            URLClassLoader loader = getCliAdapterClassLoader(coreLoader);
            Class<?> clazz = Class.forName("org.jboss.galleon.cli.core.ProvisioningSession", true, loader);
            ctx = (GalleonCommandExecutionContext) clazz.getConstructor().newInstance();
            ctx.init(session, loader);
            contexts.put(version, ctx);
        }
        return ctx;
    }

    // Called by test.
    static synchronized void removeCoreContext(String version) {
        contexts.remove(version);
    }

    private static synchronized URLClassLoader getCliAdapterClassLoader(URLClassLoader coreLoader) throws ProvisioningException {
        String apiVersion = APIVersion.getVersion();
        try {
            Path corePath = Files.createTempDirectory("galleon-core-cli-base-dir");
            corePath.toFile().deleteOnExit();
            // Handle local core
            File defaultCore = corePath.resolve("galleon-cli-core-adapter.jar").toFile();
            try (InputStream input = PmSession.class.getClassLoader().getResourceAsStream("galleon-cli-core-adapter-" + apiVersion + ".jar")) {
                try (OutputStream output = new FileOutputStream(defaultCore, false)) {
                    input.transferTo(output);
                }
            }
            defaultCore.deleteOnExit();
            URL[] cp = new URL[1];
            try {
                cp[0] = defaultCore.toURI().toURL();
                return new URLClassLoader(cp, coreLoader);
            } catch (Exception ex) {
                throw new ProvisioningException(ex);
            }
        } catch (IOException ex) {
            throw new ProvisioningException(ex);
        }
    }

    public GalleonCommandExecutionContext getGalleonContext(String coreVersion) throws ProvisioningException, CommandExecutionException {
        try {
            if(coreVersion == null) {
                coreVersion = APIVersion.getVersion();
            }
            return getCoreContext(coreVersion, this, galleonBuilder);
        } catch (Exception ex) {
            throw new CommandExecutionException(ex.getLocalizedMessage());
        }
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

    public void throwException() {
        rethrow = true;
    }

    public boolean isExceptionRethrown() {
        return rethrow;
    }

    public void close() {
        universe.close();
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

    public MessageWriter getMessageWriter(boolean verbose) {
        return new DefaultMessageWriter(out, err, verbose);
    }

//    public ResourceResolver getResolver() {
//        return resolver;
//    }
    public UniverseManager getUniverse() {
        return universe;
    }

    public void commandStart(PmCommandInvocation session) {
        maven.commandStart();
    }

    public void commandEnd(PmCommandInvocation session) {
        maven.commandEnd();
    }

    public void setToolMode(ToolModes.Mode mode, String name) {
        if (ToolModes.Mode.NOMINAL.equals(mode)) {
            toolModes.setMode(ToolModes.Mode.NOMINAL);
            promptRoot = null;
        } else {
            toolModes.setMode(ToolModes.Mode.EDIT);
            promptRoot = EDIT_MODE_PROMPT + (name == null ? ""
                    : name + "!");
        }
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

    public void enableMavenTrace(boolean b) {
        mavenListener.setActive(b);
    }

    void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean isAnsiSupported() {
        return connection != null && connection.supportsAnsi();
    }
}
