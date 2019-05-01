/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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

import java.util.Objects;
import org.aesh.command.Command;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmSessionCommand;

/**
 *
 * @author jdenise@redhat.com
 */
public class CommandDomain implements Comparable<CommandDomain> {

    public static final CommandDomain PROVISIONING = new CommandDomain(0,
            HelpDescriptions.DOMAIN_PROVISIONING);
    public static final CommandDomain INSTALLATION = new CommandDomain(10,
            HelpDescriptions.DOMAIN_INSTALLATION);
    public static final CommandDomain FEATURE_PACK = new CommandDomain(20,
            HelpDescriptions.DOMAIN_FEATURE_PACK);
    public static final CommandDomain CONFIGURATION = new CommandDomain(30,
            HelpDescriptions.DOMAIN_CONFIGURATION);
    public static final CommandDomain STATE_MODE = new CommandDomain(40,
            HelpDescriptions.DOMAIN_EDIT_MODE);
    public static final CommandDomain EDITING = new CommandDomain(50,
            HelpDescriptions.DOMAIN_EDITING);
    public static final CommandDomain OTHERS = new CommandDomain(60,
            HelpDescriptions.DOMAIN_OTHER);

    private final String description;
    private final Integer ordinal;

    private CommandDomain(int ordinal, String description) {
        this.ordinal = ordinal;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static CommandDomain getDomain(Command command) {
        if (command instanceof AbstractDynamicCommand) {
            AbstractDynamicCommand dyn = (AbstractDynamicCommand) command;
            return dyn.getDomain();
        }
        if (command instanceof PmSessionCommand) {
            PmSessionCommand pm = (PmSessionCommand) command;
            return pm.getDomain();
        }
        if (command instanceof PmGroupCommand) {
            PmGroupCommand grp = (PmGroupCommand) command;
            return grp.getDomain();
        }
        return OTHERS;
    }

    @Override
    public int compareTo(CommandDomain o) {
        return ordinal.compareTo(o.ordinal);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CommandDomain)) {
            return false;
        }
        CommandDomain cd = (CommandDomain) other;
        return ordinal.equals(cd.ordinal);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.ordinal);
        return hash;
    }
}
