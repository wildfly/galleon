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
package org.jboss.galleon.cli;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jdenise@redhat.com
 */
public class ArgumentsTestCase {
    @Test
    public void test() {
        {
            Arguments args = Arguments.parseArguments(null);
            assertNull(args.getCommand());
            assertTrue(args.getOptions().isEmpty());
            assertFalse(args.isHelp());
        }
        {
            Arguments args = Arguments.parseArguments(new String[0]);
            assertNull(args.getCommand());
            assertTrue(args.getOptions().isEmpty());
            assertFalse(args.isHelp());
        }
        {
            String[] arr = {Arguments.HELP};
            Arguments args = Arguments.parseArguments(arr);
            assertTrue(args.isHelp());
        }
        {
            String[] arr = {Arguments.SCRIPT_FILE + "foo-file.cli"};
            Arguments args = Arguments.parseArguments(arr);
            assertEquals("foo-file.cli", args.getScriptFile());
        }
        {
            String[] arr = {Arguments.HELP, "foo"};
            Arguments args = Arguments.parseArguments(arr);
            assertTrue(args.isHelp());
        }
        {
            Map<String, String> expected = new HashMap<>();
            expected.put("opt1", null);
            expected.put("opt2", "foo");
            String[] arr = {"--opt1", "--opt2=foo", "foo", "--bar", "arg"};
            Arguments args = Arguments.parseArguments(arr);
            assertEquals(args.getCommand(), "foo --bar arg");
            assertEquals(expected, args.getOptions());
            assertFalse(args.isHelp());
        }
        {
            String[] arr = {"foo", "--bar", "--arg"};
            Arguments args = Arguments.parseArguments(arr);
            assertEquals(args.getCommand(), "foo --bar --arg");
            assertTrue(args.getOptions().isEmpty());
            assertFalse(args.isHelp());
        }
        {
            Map<String, String> expected = new HashMap<>();
            expected.put("opt1", null);
            String[] arr = {"--opt1"};
            Arguments args = Arguments.parseArguments(arr);
            assertNull(args.getCommand());
            assertEquals(expected, args.getOptions());
            assertFalse(args.isHelp());
        }

    }
}
