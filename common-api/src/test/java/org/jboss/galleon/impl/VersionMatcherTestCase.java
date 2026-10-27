/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.impl;

import static org.jboss.galleon.impl.VersionMatcher.getLatestVersion;

import java.util.Set;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertThrows;
import org.junit.Test;

public class VersionMatcherTestCase {

    @Test
    public void test1() {
        assertLatestVersion("3",
                "1", "3", "2");
    }

    @Test
    public void test2() {
        assertLatestVersion("202109080827",
                "1", "202009080827", "202109080827");
    }

    @Test
    public void test3() {
        assertLatestVersion("1.1.4",
                "1.0.0", "1.0.2", "1.1.2", "1.1.4");
    }

    @Test
    public void test4() {
        assertLatestVersion("1.2",
                "1.0", "1.0.2", "1.1", "1.2");
    }

    @Test
    public void test5() {
        assertLatestVersion("Z",
                "A", "B", "C", "Z");
    }

    @Test
    public void test6() {
        assertLatestVersion("1.0.1.GA",
                "1.0.GA", "1.0.1.GA");
    }

    @Test
    public void test7() {
        assertLatestVersion("1.0.0.Beta",
                "1.0.0.Alpha", "1.0.0.Beta");
    }

    @Test
    public void test8() {
        assertLatestVersion("1:1",
                "0:1", "1:1");
    }

    @Test
    public void test9() {
        assertLatestVersion("2.1.9.redhat-1",
                "2.1.9.redhat-001", "2.1.9.redhat-1");
    }

    @Test
    public void test10() {
        assertLatestVersion("2.1.9.redhat-2",
                "2.1.9.redhat-001", "2.1.9.redhat-2");
    }

    @Test
    public void test11() {
        assertLatestVersion("2.1.9.redhat-002",
                "2.1.9.redhat-1", "2.1.9.redhat-002");
    }

    @Test
    public void test12() {
        assertLatestVersion("1.0.0.Final",
                "1.0.0", "1.0.0.Final");
    }

    @Test
    public void test13() {
        assertLatestVersion("1.0.0.Final",
                "1.0.0.Alpha", "1.0.0.Final");
    }

    @Test
    public void test14() {
        // 1.0.0.Redhat-1 is the latest, because "r" is after "G"
        assertLatestVersion("1.0.0.Redhat-1",
                "1.0.0.Redhat-1", "1.0.0.GA-redhat-1");
    }

    @Test
    public void test15() {
        assertLatestVersion("1.0.10",
                "1.0.2", "1.0.10");
    }

    @Test
    public void test16() {
        // 1.0.0.RC1 is the latest because "R" is after "G"
        assertLatestVersion("1.0.0.RC1",
                "1.0.0.GA", "1.0.0.RC1");
    }

    @Test
    public void test17() {
        assertLatestVersion("1.0.0.GA",
                "1.0.0.GA", "1.0.0.CR1");
    }

    @Test
    public void test18() {
        assertLatestVersion("1.10",
                "1.1", "1.10");
    }

    @Test
    public void test19() {
        assertLatestVersion("1.1",
                "1.01", "1.1");
    }

    @Test
    public void test20() {
        // epoch must be a positive integer
        assertThrows(NumberFormatException.class, () -> {
            getLatestVersion(Set.of("1.0:alpha", "2.0:alpha"));
        });
    }

    @Test
    public void test21() {
        // 2 epoch is later than 1 epoch
        assertLatestVersion("2:1.1",
                "1:1.1", "2:1.1");
    }

    @Test
    public void test22() {
        // SP1 is a service pack on top of the base version, so 4.0.11.SP1-redhat-00001 must be the latest
        assertLatestVersion("4.0.11.SP1-redhat-00001",
                "4.0.11.redhat-00001", "4.0.11.SP1-redhat-00001");
    }

    private static void assertLatestVersion(String expectedLatest, String... versions) {
        assertEquals(expectedLatest, getLatestVersion(Set.of(versions)).get());
    }
}
