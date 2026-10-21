/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.core;

import java.util.List;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCompleterInvocation;

/**
 *
 * @author jdenise
 */
public interface GalleonCoreContentCompleter<C extends GalleonCommandExecutionContext> {
    List<String> complete(PmCompleterInvocation invoc, C context);
}
