/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
