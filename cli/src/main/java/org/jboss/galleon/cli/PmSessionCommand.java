/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli;

import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmSessionCommand implements Command<PmCommandInvocation> {

    @Override
    public CommandResult execute(PmCommandInvocation session) throws CommandException {
        try {
            runCommand(session);
            return CommandResult.SUCCESS;
        } catch (Throwable t) {
            //t.printStackTrace();
            if(t instanceof RuntimeException) {
                t.printStackTrace(session.getErr());
            }

            session.print("Error: ");
            println(session, t);

            t = t.getCause();
            int offset = 1;
            while(t != null) {
                for(int i = 0; i < offset; ++i) {
                    session.print(" ");
                }
                session.print("* ");
                println(session, t);
                t = t.getCause();
                ++offset;
            }
            return CommandResult.FAILURE;
        }
    }

    private void println(PmCommandInvocation session, Throwable t) {
        if(t.getLocalizedMessage() == null) {
            session.println(t.getClass().getName());
        } else {
            session.println(t.getLocalizedMessage());
        }
    }

    protected abstract void runCommand(PmCommandInvocation session) throws CommandExecutionException;
}
