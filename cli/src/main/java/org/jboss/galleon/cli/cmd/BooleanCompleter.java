/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
