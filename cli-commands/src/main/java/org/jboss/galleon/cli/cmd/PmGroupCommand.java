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
package org.jboss.galleon.cli.cmd;

import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.jboss.galleon.cli.PmCommandInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
public interface PmGroupCommand extends Command<PmCommandInvocation> {

    CommandDomain getDomain();

    default CommandResult execute(PmCommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.println(CliErrors.subCommandMissing());
        return CommandResult.FAILURE;
    }
}
