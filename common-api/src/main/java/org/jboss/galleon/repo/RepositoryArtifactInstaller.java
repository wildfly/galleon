/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.repo;

import java.nio.file.Path;
import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author jdenise@redhat.com
 */
public interface RepositoryArtifactInstaller {
    void install(String coords, Path artifact) throws ProvisioningException;
}
