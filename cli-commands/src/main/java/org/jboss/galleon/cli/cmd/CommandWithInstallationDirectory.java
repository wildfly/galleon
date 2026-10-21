/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import java.nio.file.Path;
import org.aesh.readline.AeshContext;

/**
 *
 * @author jdenise@redhat.com
 */
public interface CommandWithInstallationDirectory {

    Path getInstallationDirectory(AeshContext ctx);
}
