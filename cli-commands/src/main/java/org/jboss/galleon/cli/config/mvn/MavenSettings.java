/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.config.mvn;

import java.util.List;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 *
 * @author jdenise@redhat.com
 */
public interface MavenSettings {
    List<RemoteRepository> getRepositories();
    RepositorySystemSession getSession();
}
