/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.core;

import java.util.List;
import java.util.Map;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCLIDynamicCommand;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.cmd.AbstractDynamicCommand;

/**
 *
 * @author jdenise
 */
public interface GalleonCoreDynamicExecution<C extends GalleonCommandExecutionContext, T extends GalleonCLIDynamicCommand> {
    void execute(C context, T command, Map<String, String> options) throws CommandExecutionException;
    List<AbstractDynamicCommand.DynamicOption> getDynamicOptions(C session, T cmd) throws Exception;
}
