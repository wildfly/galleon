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
package org.jboss.galleon.cli.resolver;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.ProvisioningOption;

/**
 *
 * @author jdenise@redhat.com
 */
public class ResolvedPlugins {

    private final Set<ProvisioningOption> install;
    private final Set<ProvisioningOption> diff;

    ResolvedPlugins(Set<ProvisioningOption> install, Set<ProvisioningOption> diff) {
        this.install = Collections.unmodifiableSet(install);
        this.diff = Collections.unmodifiableSet(diff);
    }

    /**
     * @return the install
     */
    public Set<ProvisioningOption> getInstall() {
        return install;
    }

    /**
     * @return the diff
     */
    public Set<ProvisioningOption> getDiff() {
        return diff;
    }

}
