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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aesh.command.impl.internal.ParsedCommand;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise
 */
public interface GalleonCommandExecutionContext {

    void init(PmSession session, ClassLoader loader) throws ProvisioningException;

    void execute(GalleonCLICommand cmd, PmCommandInvocation invoc) throws CommandExecutionException;

    public void executeDynamic(GalleonCLIDynamicCommand cmd, PmCommandInvocation invoc, Map<String, String> options) throws CommandExecutionException;

    public FeaturePackLocation getExposedLocation(Path installation, FeaturePackLocation fplocation);

    public FeaturePackLocation getResolvedLocation(Path installation, String location) throws ProvisioningException;

    public List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(GalleonCLIDynamicCommand cmd) throws CommandExecutionException;

    public Set<String> getLayers(String model, FeaturePackLocation loc, Set<String> excluded) throws CommandExecutionException;

    public UniverseSpec getDefaultUniverseSpec(Path installation);

    public Set<String> getUniverseNames(Path installation);

    public UniverseSpec getUniverseSpec(Path installation, String name);

    public List<FeaturePackLocation> getInstallationLocations(Path installation, boolean transitive, boolean patches);

    public boolean isTrackersEnabled();

    public ProgressTracker<FeaturePackLocation.FPID> newFindTracker(PmCommandInvocation invoc);

    public void unregisterTrackers();

    public void visitAllUniverses(UniverseManager.UniverseVisitor visitor, boolean b, Path finalPath);

    public void complete(GalleonCLICommandCompleter cmd, PmCompleterInvocation invoc);
    public List<String> completionContent(GalleonCLICommandCompleter cmd, PmCompleterInvocation invoc);
    public boolean isActivated(GalleonCLICommandActivator activator, ParsedCommand command);
}
