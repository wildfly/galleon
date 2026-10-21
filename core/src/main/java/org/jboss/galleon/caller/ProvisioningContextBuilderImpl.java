/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.caller;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.core.builder.LocalFP;
import org.jboss.galleon.core.builder.ProvisioningContext;
import org.jboss.galleon.core.builder.ProvisioningContextBuilder;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseResolver;

public class ProvisioningContextBuilderImpl implements ProvisioningContextBuilder {

    @Override
    public ProvisioningContext buildProvisioningContext(URLClassLoader loader, Path home,
            MessageWriter msgWriter,
            boolean logTime,
            boolean recordState,
            UniverseResolver universeResolver,
            Map<String, ProgressTracker<?>> progressTrackers,
            Map<FPID, LocalFP> locals) throws ProvisioningException {
        return buildContext(loader, home, msgWriter, logTime, recordState, universeResolver, progressTrackers, locals);
    }

    private static ProvisioningContext buildContext(URLClassLoader loader, Path home,
            MessageWriter msgWriter,
            boolean logTime,
            boolean recordState,
            UniverseResolver universeResolver,
            Map<String, ProgressTracker<?>> progressTrackers,
            Map<FPID, LocalFP> locals) throws ProvisioningException  {
        return new ProvisioningContextImpl(loader, home, msgWriter, logTime, recordState, universeResolver, progressTrackers, locals);
    }
}
