/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.core;

import org.aesh.command.impl.internal.ParsedCommand;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;

/**
 *
 * @author jdenise
 */
public interface GalleonCoreActivator<C extends GalleonCommandExecutionContext> {

    Boolean isActivated(ParsedCommand command, C context);
}
