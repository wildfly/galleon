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
package org.jboss.galleon.cli.cmd.state;

import org.aesh.command.CommandException;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.GalleonCommandExecutionContext;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.PmSessionCommand;
import org.jboss.galleon.cli.cmd.CommandDomain;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractStateCommand extends PmSessionCommand {

    @Override
    protected void runCommand(PmCommandInvocation invoc) throws CommandExecutionException {
        throw new CommandExecutionException("Shouldn't have been called");
    }

    @Override
    public GalleonCommandExecutionContext getGalleonContext(PmSession session) throws CommandException {
        return getGalleonExecutionContext(session);
    }

    public static GalleonCommandExecutionContext getGalleonExecutionContext(PmSession session) throws CommandException {
        GalleonCommandExecutionContext ctx = session.getState();
        if (ctx == null) {
            throw new CommandException("No state exist, command not available");
        }
        return ctx;
    }

    @Override
    public CommandDomain getDomain() {
        return CommandDomain.EDITING;
    }
}
