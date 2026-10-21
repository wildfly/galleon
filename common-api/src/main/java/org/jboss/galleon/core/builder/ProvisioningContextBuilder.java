/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.core.builder;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.UniverseResolver;

public interface ProvisioningContextBuilder {

    public ProvisioningContext buildProvisioningContext(URLClassLoader loader, Path home,
            MessageWriter msgWriter,
            boolean logTime,
            boolean recordState,
            UniverseResolver resolver,
            Map<String, ProgressTracker<?>> progressTrackers, Map<FPID, LocalFP> locals) throws ProvisioningException;
}
