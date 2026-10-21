/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.core;

import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCompleterInvocation;

/**
 *
 * @author jdenise
 */
public interface GalleonCoreCompleter<C extends GalleonCommandExecutionContext> {
    void complete(PmCompleterInvocation invoc, C context);
}
