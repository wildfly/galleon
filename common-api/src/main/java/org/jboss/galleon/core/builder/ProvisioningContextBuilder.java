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
