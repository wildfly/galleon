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
package org.jboss.galleon.cli.cmd.state.pkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.state.State;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionedPackageCommand extends AbstractFPProvisionedCommand {

    public static class ProvisionedPackageCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            try {
                List<String> packages = new ArrayList<>();
                AbstractProvisionedPackageCommand cmd = (AbstractProvisionedPackageCommand) completerInvocation.getCommand();
                FeaturePackConfig fp = cmd.getProvisionedFP(completerInvocation.getPmSession());
                if (fp == null) {
                    // We want them all from all FP
                    for (FeaturePackConfig fc : completerInvocation.getPmSession().getState().getConfig().getFeaturePackDeps()) {
                        for (String pkg : cmd.getTargetedPackages(fc)) {
                            if (!packages.contains(pkg)) {
                                packages.add(pkg);
                            }
                        }
                    }
                } else {
                    for (String pkg : cmd.getTargetedPackages(fp)) {
                        if (!packages.contains(pkg)) {
                            packages.add(pkg);
                        }
                    }
                }
                return packages;
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }

    }
    public static class TargetedFPCompleter extends AbstractCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            try {
                State session = completerInvocation.getPmSession().getState();
                List<String> lst = new ArrayList<>();
                if (session != null) {
                    AbstractProvisionedPackageCommand cmd = (AbstractProvisionedPackageCommand) completerInvocation.getCommand();
                    String pkg = cmd.getPackage();
                    for (FeaturePackConfig fc : session.getConfig().getFeaturePackDeps()) {
                        if (cmd.getTargetedPackages(fc).contains(pkg)) {
                            lst.add(Identity.buildOrigin(fc.getLocation().getProducer()));
                        }
                    }
                }
                return lst;
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }

    }
    @Argument(required = true, description = "Package name",
            completer = ProvisionedPackageCompleter.class)
    private String pkg;

    @Option(completer = TargetedFPCompleter.class)
    protected String origin;

    @Override
    public ProducerSpec getProducer(PmSession session) throws CommandExecutionException {
        if (origin == null) {
            return null;
        }
        return LegacyGalleon1Universe.toFpl(ArtifactCoords.newGav(origin)).getProducer();
    }

    protected String getPackage() {
        return pkg;
    }

    protected String getPackage(PmSession session) throws CommandExecutionException {
        FeaturePackConfig cf = getProvisionedFP(session);
        Set<String> ids = getTargetedPackages(cf);
        if (ids.contains(pkg)) {
            return pkg;
        }
        return null;
    }

    protected abstract Set<String> getTargetedPackages(FeaturePackConfig cf);

}
