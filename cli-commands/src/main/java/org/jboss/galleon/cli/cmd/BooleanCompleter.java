/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import java.util.Arrays;
import java.util.List;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.CommandExecutionException;
import org.jboss.galleon.cli.PmCompleterInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class BooleanCompleter extends AbstractCompleter {

    private static final List<String> VALUES = Arrays.asList("false", "true");

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        return VALUES;
    }

    public static Boolean validateValue(String value) throws CommandExecutionException {
        if (!VALUES.contains(value)) {
            throw new CommandExecutionException(CliErrors.invalidBoolean(value));
        }
        return Boolean.parseBoolean(value);
    }
}
