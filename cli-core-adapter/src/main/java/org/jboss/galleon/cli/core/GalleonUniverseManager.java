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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.cli.UniverseManager;
import org.jboss.galleon.cli.cmd.CliErrors;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.jboss.galleon.util.PathsUtils;

/**
 *
 * @author jdenise@redhat.com
 */
public class GalleonUniverseManager {

    private final UniverseManager mgr;
    private final ProvisioningSession session;

    GalleonUniverseManager(ProvisioningSession session, UniverseManager mgr) throws ProvisioningException {
        this.session = session;
        this.mgr = mgr;
    }

    public FeaturePackLocation resolveLatestBuild(FeaturePackLocation fpl) throws ProvisioningException {
        return mgr.resolveLatestBuild(fpl);
    }

    private ProvisioningManager getProvisioningManager(Path installation) throws ProvisioningException {
        if (installation == null) {
            throw new ProvisioningException(CliErrors.noDirectoryProvided());
        }
        if (!Files.exists(PathsUtils.getProvisioningXml(installation))) {
            throw new ProvisioningException(CliErrors.notValidInstallation(installation));
        }
        ProvisioningManager mgr = session.newProvisioningManager(installation, false);
        return mgr;
    }

    public void addUniverse(String name, String factory, String location) throws ProvisioningException, IOException {
        UniverseSpec u = new UniverseSpec(factory, location);
        session.getState().addUniverse(session, name, factory, location);
        resolveUniverse(u);
    }

    public void addUniverse(Path installation, String name, String factory, String location) throws ProvisioningException, IOException {
        UniverseSpec u = new UniverseSpec(factory, location);
        ProvisioningManager mgr = getProvisioningManager(installation);

        if (name != null) {
            mgr.addUniverse(name, u);
        } else {
            mgr.setDefaultUniverse(u);
        }
        resolveUniverse(u);
    }

    private void resolveUniverse(UniverseSpec u) throws ProvisioningException {
        mgr.resolveUniverse(u);
    }

    public void removeUniverse(String name) throws ProvisioningException, IOException {
        session.getState().removeUniverse(session, name);
    }

    public void removeUniverse(Path installation, String name) throws ProvisioningException, IOException {
        ProvisioningManager mgr = getProvisioningManager(installation);
        // Remove default if name is null
        mgr.removeUniverse(name);
    }

    public Set<String> getUniverseNames(Path installation) {
        if (session.getState() != null) {
            return session.getState().getConfig().getUniverseNamedSpecs().keySet();
        }
        try {
            ProvisioningManager mgr = getProvisioningManager(installation);
            return mgr.getProvisioningConfig().getUniverseNamedSpecs().keySet();
        } catch (ProvisioningException ex) {
            return Collections.emptySet();
        }
    }

    public UniverseSpec getDefaultUniverseSpec(Path installation) {
        UniverseSpec defaultUniverse = null;
        if (session.getState() != null) {
            defaultUniverse = session.getState().getConfig().getDefaultUniverse();
        } else {
            try {
                ProvisioningManager mgr = getProvisioningManager(installation);
                defaultUniverse = mgr.getProvisioningConfig().getDefaultUniverse();
            } catch (ProvisioningException ex) {
                // OK, not an installation
            }
        }
        return defaultUniverse == null ? mgr.getBuiltinUniverseSpec() : defaultUniverse;
    }

    public String getUniverseName(Path installation, UniverseSpec u) {
        ProvisioningConfig config = null;
        if (session.getState() != null) {
            config = session.getState().getConfig();
        } else {
            try {
                config = getProvisioningManager(installation).getProvisioningConfig();
            } catch (ProvisioningException ex) {
                return null;
            }
        }
        for (Map.Entry<String, UniverseSpec> entry : config.getUniverseNamedSpecs().entrySet()) {
            if (entry.getValue().equals(u)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public UniverseSpec getUniverseSpec(Path installation, String name) {
        ProvisioningConfig config;
        if (session.getState() != null) {
            config = session.getState().getConfig();
        } else {
            try {
                config = getProvisioningManager(installation).getProvisioningConfig();
            } catch (ProvisioningException ex) {
                return null;
            }
        }
        return config.getUniverseNamedSpecs().get(name);
    }

    public void visitUniverse(UniverseSpec universeSpec,
            UniverseManager.UniverseVisitor visitor, boolean allBuilds) throws ProvisioningException {
        mgr.visit(visitor, mgr.getUniverse(universeSpec), universeSpec, allBuilds);
    }

    public void visitAllUniverses(UniverseManager.UniverseVisitor visitor,
            boolean allBuilds, Path installation) {
        try {
            mgr.visit(visitor, mgr.getUniverse(mgr.getBuiltinUniverseSpec()), mgr.getBuiltinUniverseSpec(), allBuilds);
        } catch (ProvisioningException ex) {
            visitor.exception(mgr.getBuiltinUniverseSpec(), ex);
        }
        UniverseSpec defaultUniverse = getDefaultUniverseSpec(null);
        try {
            if (defaultUniverse != null && !mgr.getBuiltinUniverseSpec().equals(defaultUniverse)) {
                mgr.visit(visitor, mgr.getUniverse(defaultUniverse), defaultUniverse, allBuilds);
            }
        } catch (ProvisioningException ex) {
            visitor.exception(defaultUniverse, ex);
        }
        Set<String> universes = getUniverseNames(installation);
        for (String u : universes) {
            UniverseSpec universeSpec = getUniverseSpec(installation, u);
            try {
                mgr.visit(visitor, mgr.getUniverse(universeSpec), universeSpec, allBuilds);
            } catch (ProvisioningException ex) {
                visitor.exception(universeSpec, ex);
            }
        }
    }

    void resolve(FeaturePackLocation resolvedLocation) throws ProvisioningException {
        mgr.resolve(resolvedLocation);
    }

}
