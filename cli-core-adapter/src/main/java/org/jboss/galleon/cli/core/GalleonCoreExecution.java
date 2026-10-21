/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.core;

import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCLICommand;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;

/**
 *
 * @author jdenise
 */
public interface GalleonCoreExecution<C extends GalleonCommandExecutionContext, T extends GalleonCLICommand> {
    void execute(C context, T command) throws CommandExecutionException;
}
