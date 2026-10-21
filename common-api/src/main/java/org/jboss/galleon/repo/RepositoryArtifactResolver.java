/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.repo;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;

/**
 *
 * @author Alexey Loubyansky
 */
public interface RepositoryArtifactResolver {

    String ID_PREFIX = "repository.";

    String getRepositoryId();

    Path resolve(String location) throws ProvisioningException;
}
