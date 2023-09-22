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
package org.jboss.galleon.cli.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.internal.ParsedCommand;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCLICommand;
import org.jboss.galleon.cli.GalleonCLICommandActivator;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.GalleonCLIDynamicCommand;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.ToolModes;
import org.jboss.galleon.cli.UniverseManager;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.cli.resolver.ResourceResolver;
import org.jboss.galleon.cli.tracking.ProgressTrackers;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.layout.ProvisioningLayout;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1UniverseFactory;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise
 */
public class ProvisioningSession implements GalleonCommandExecutionContext {

    private ProvisioningLayoutFactory layoutFactory;
    private State state;
    private PmSession session;
    private ClassLoader loader;
    private PmCommandInvocation invoc;
    private String currentPath;
    private boolean enableTrackers = true;
    private boolean commandRunning;
    private GalleonUniverseManager universe;
    private ResourceResolver resolver;

    @Override
    public void init(PmSession session, ClassLoader loader) throws ProvisioningException {
        layoutFactory = layoutFactory
                = ProvisioningLayoutFactory.getInstance(session.getUniverseResolver());
        this.session = session;
        this.loader = loader;
        this.universe = new GalleonUniverseManager(this, session.getUniverse());
        resolver = new ResourceResolver();
    }

    public ResourceResolver getResolver() {
        return resolver;
    }

    public void close() {
        try {
            if (state != null) {
                state.close();
            }
        } finally {
            if (session.isInteractive()) {
                layoutFactory.checkOpenLayouts();
            } else {
                layoutFactory.close();
            }
        }
    }

    public PmCommandInvocation getCommandInvocation() {
        return invoc;
    }

    public void commandStart(PmCommandInvocation invoc) {
        this.invoc = invoc;
        commandRunning = true;
        ProgressTrackers.commandStart(this, invoc);
        registerTrackers();
    }

    public void commandEnd(PmCommandInvocation invoc) {
        this.invoc = null;
        ProgressTrackers.commandEnd(this, invoc);
        unregisterTrackers();
        commandRunning = false;
    }

    public void downloadFp(FeaturePackLocation.FPID fpid) throws ProvisioningException {
        universe.resolve(getResolvedLocation(null, fpid.getLocation()));
    }

    @Override
    public FeaturePackLocation getResolvedLocation(Path installation, String location) throws ProvisioningException {
        final char endC = location.charAt(location.length() - 1);
        if (endC == FeaturePackLocation.FREQUENCY_START
                || endC == FeaturePackLocation.BUILD_START) {
            location = location.substring(0, location.length() - 1);
        }
        // A producer spec without any universe nor channel.
        /*
        if (!location.contains("" + FeaturePackLocation.UNIVERSE_START) && !location.contains("" + FeaturePackLocation.CHANNEL_START)) {
            location = new FeaturePackLocation(universe.getDefaultUniverseSpec(installation), location, null, null, null).toString();
        }
         */
        FeaturePackLocation loc = FeaturePackLocation.fromString(location);
        if (loc.getUniverse() == null) {
            loc = loc.replaceUniverse(universe.getDefaultUniverseSpec(installation));
        }
        return getResolvedLocation(installation, loc);
    }

    public FeaturePackLocation getResolvedLocation(Path installation, FeaturePackLocation fplocation) throws ProvisioningException {
        UniverseSpec spec = fplocation.getUniverse();
        if (spec != null) {
            if (fplocation.isMavenCoordinates() || LegacyGalleon1UniverseFactory.ID.equals(spec.getFactory())) {
                return fplocation;
            }
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

    @Override
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

    public GalleonUniverseManager getUniverse() {
        return universe;
    }

    public void enableTrackers(boolean enable) {
        enableTrackers = enable;
    }

    @Override
    public boolean isTrackersEnabled() {
        return enableTrackers;
    }

    public void registerTrackers() {
        if (enableTrackers && commandRunning) {
            ProgressTrackers.registerTrackers(this);
        }
    }

    @Override
    public ProgressTracker<FeaturePackLocation.FPID> newFindTracker(PmCommandInvocation invoc) {
        return ProgressTrackers.newFindTracker(this,invoc);
    }

    @Override
    public void unregisterTrackers() {
        ProgressTrackers.unregisterTrackers(this);
    }

    @Override
    public void visitAllUniverses(UniverseManager.UniverseVisitor visitor, boolean b, Path finalPath) {
        universe.visitAllUniverses(visitor, b, finalPath);
    }

    public MessageWriter getMessageWriter(boolean verbose) {
        if (enableTrackers) {
            // In verbose mode, do not mix verbose content with the output from progress tracker.
            if (verbose) {
                unregisterTrackers();
            }
        }
        return session.getMessageWriter(verbose);
    }

    public void setState(State state) {
        if (state == null) {
            if (this.state != null) {
                this.state.close();
            }
            session.setToolMode(ToolModes.Mode.NOMINAL, null);
            setCurrentPath(null);
        } else {
            session.setToolMode(ToolModes.Mode.EDIT, state.getName());
        }
        session.setState(this);
        this.state = state;
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

    public State getState() {
        return state;
    }

    public FeatureContainer getContainer() {
        if (state != null) {
            return state.getContainer();
        }
        return null;
    }

    public PmSession getPmSession() {
        return session;
    }

    public ProvisioningLayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public ProvisioningManager newProvisioningManager(Path installation, boolean verbose) throws ProvisioningException {
        ProvisioningManager.Builder builder = ProvisioningManager.builder();
        builder.setLayoutFactory(getLayoutFactory());
        if (installation != null) {
            builder.setInstallationHome(installation);
        }
        builder.setMessageWriter(session.getMessageWriter(verbose));
        return builder.build();
    }

    @Override
    public void execute(GalleonCLICommand cmd, PmCommandInvocation invoc) throws CommandExecutionException {
        try {
            commandStart(invoc);
            String getCmdClassName = cmd.getCommandClassName(invoc.getPmSession());
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreExecution obj = (GalleonCoreExecution) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                obj.execute(this, cmd);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(session, ex.getMessage(), ex);
        } finally {
            commandEnd(invoc);
        }
    }
    @Override
    public void complete(GalleonCLICommandCompleter cmd, PmCompleterInvocation invoc) {
        try {
            String getCmdClassName = cmd.getCoreCompleterClassName(invoc.getPmSession());
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreCompleter obj = (GalleonCoreCompleter) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                obj.complete(invoc, this);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
        }
    }

    public List<String> completionContent(GalleonCLICommandCompleter cmd, PmCompleterInvocation invoc) {
        try {
            String getCmdClassName = cmd.getCoreCompleterClassName(invoc.getPmSession());
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreContentCompleter obj = (GalleonCoreContentCompleter) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                return obj.complete(invoc, this);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isActivated(GalleonCLICommandActivator activator, ParsedCommand command) {
        try {
            String getCmdClassName = activator.getCoreActivatorClassName();
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreActivator obj = (GalleonCoreActivator) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                return obj.isActivated(command, this);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            CliLogging.exception(ex);
        }
        return false;
    }

    @Override
    public void executeDynamic(GalleonCLIDynamicCommand cmd, PmCommandInvocation invoc, Map<String, String> options) throws CommandExecutionException {
        try {
            commandStart(invoc);
            String getCmdClassName = cmd.getCommandClassName(invoc.getPmSession());
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreDynamicExecution obj = (GalleonCoreDynamicExecution) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                obj.execute(this, cmd, options);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(session, ex.getMessage(), ex);
        } finally {
            commandEnd(invoc);
        }
    }

    @Override
    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(GalleonCLIDynamicCommand cmd) throws CommandExecutionException {
        try {
            String getCmdClassName = cmd.getCommandClassName(session);
            Class<?> clazz = Class.forName(getCmdClassName, true, loader);
            GalleonCoreDynamicExecution obj = (GalleonCoreDynamicExecution) clazz.getConstructor().newInstance();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                return obj.getDynamicOptions(this, cmd);
            } finally {
                Thread.currentThread().setContextClassLoader(l);
            }
        } catch (Exception ex) {
            throw new CommandExecutionException(this.getPmSession(), ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public Set<String> getLayers(String model, FeaturePackLocation loc, Set<String> excluded) throws CommandExecutionException {
        try {
            return LayersConfigBuilder.getLayerNames(this, model,
                    loc, excluded);
        } catch (IOException | ProvisioningException ex) {
            throw new CommandExecutionException(this.getPmSession(), ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public UniverseSpec getDefaultUniverseSpec(Path installation) {
        return universe.getDefaultUniverseSpec(installation);
    }

    @Override
    public Set<String> getUniverseNames(Path installation) {
        return universe.getUniverseNames(installation);
    }

    @Override
    public UniverseSpec getUniverseSpec(Path installation, String name) {
        return universe.getUniverseSpec(installation, name);
    }

    @Override
    public List<FeaturePackLocation> getInstallationLocations(Path installation, boolean transitive, boolean patches) {
        List<FeaturePackLocation> items = new ArrayList<>();
        try {
            PathsUtils.assertInstallationDir(installation);
            ProvisioningManager mgr
                    = newProvisioningManager(installation, false);
            try (ProvisioningLayout<FeaturePackLayout> layout = mgr.getLayoutFactory().newConfigLayout(mgr.getProvisioningConfig())) {
                for (FeaturePackLayout fp : layout.getOrderedFeaturePacks()) {
                    if (fp.isDirectDep() || (fp.isTransitiveDep() && transitive)) {
                        items.add(fp.getFPID().getLocation());
                    }
                    if (patches) {
                        List<FeaturePackLayout> appliedPatches = layout.getPatches(fp.getFPID());
                        for (FeaturePackLayout patch : appliedPatches) {
                            items.add(patch.getFPID().getLocation());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            CliLogging.completionException(ex);
        }
        return items;
    }
}
