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
package org.jboss.galleon.cli;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jdenise@redhat.com
 */
public class Arguments {

    public static final String HELP = "--help";
    public static final String SCRIPT_FILE = "--file=";

    private boolean isHelp;
    private Map<String, String> options = Collections.emptyMap();
    private String command;
    private String scriptFile;

    private static final Map<String, String> OPTIONS = new HashMap<>();

    static {
        OPTIONS.put(HELP, HelpDescriptions.TOOL_HELP_OPTION);
        OPTIONS.put(SCRIPT_FILE, HelpDescriptions.TOOL_FILE_OPTION);
    }

    public static Map<String, String> getToolOptions() {
        return Collections.unmodifiableMap(OPTIONS);
    }

    private Arguments() {
    }

    public boolean isHelp() {
        return isHelp;
    }

    public String getCommand() {
        return command;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public static Arguments parseArguments(String[] args) {
        Arguments arguments = new Arguments();
        if (args == null || args.length == 0) {
            return arguments;
        }
        Map<String, String> opts = new HashMap<>();
        int i;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if (HELP.equals(arg)) {
                    arguments.isHelp = true;
                    return arguments;
                }
                if (arg.startsWith(SCRIPT_FILE)) {
                    int sep = arg.indexOf("=");
                    arguments.scriptFile = arg.substring(sep + 1);
                    continue;
                }
                if (arg.contains("=")) {
                    int sep = arg.indexOf("=");
                    String opt = arg.substring(2, sep);
                    String val = arg.substring(sep + 1);
                    opts.put(opt, val);
                } else {
                    opts.put(arg.substring(2, arg.length()), null);
                }
            } else {
                // Done for options.
                break;
            }
        }
        arguments.options = Collections.unmodifiableMap(opts);

        // remaining args are command.
        StringBuilder builder = new StringBuilder();
        for (; i < args.length; i++) {
            String cmd = args[i];
            builder.append(cmd).append(" ");
        }
        arguments.command = builder.length() == 0 ? null : builder.toString().trim();

        return arguments;
    }
}
