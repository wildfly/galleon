/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class PathFilterTest {
    /**
     * Test of accept method, of class PathFilter.
     */
    @Test
    public void testRegexpConversion() {
        String expResult = ".*_xml_history";
        String result = PathFilter.Builder.wildcardToJavaRegexp("*_xml_history").pattern();
        assertEquals(expResult, result);
    }
    /**
     * Test of accept method, of class PathFilter.
     */
    @Test
    public void testFolders() {
        PathFilter filter = PathFilter.Builder.instance()
            .addDirectories("*" + File.separatorChar + "tmp", "*" + File.separatorChar + "log","*_xml_history")
            .build();
        boolean result = filter.accept(Paths.get("standalone", "configuration", "standalone_xml_history"));
        assertEquals(false, result);
        result = filter.accept(Paths.get("standalone", "configuration", "tmp"));
        assertEquals(false, result);
        result = filter.accept(Paths.get("standalone", "configuration", "temp"));
        assertEquals(true, result);
        result = filter.accept(Paths.get("standalone", "configuration", "log"));
        assertEquals(false, result);
        result = filter.accept(Paths.get("standalone", "configuration", "logs"));
        assertEquals(true, result);
        result = filter.accept(Paths.get("standalone", "configuration", "user_tmp"));
        assertEquals(true, result);
        result = filter.accept(Paths.get("standalone", "configuration", "standalone_xml_history2"));
        assertEquals(true, result);
        filter = PathFilter.Builder.instance()
            .addFiles("*.xml")
            .build();
        result = filter.accept(Paths.get("standalone", "configuration", "standalone_xml_history", "standalone.last.xml"));
        assertEquals(false, result);
        result = filter.accept(Paths.get("standalone", "configuration", "standalone_xml_history"));
        assertEquals(true, result);
        result = filter.accept(Paths.get("standalone", "configuration", "standalone_xml_history", "test.properties"));
        assertEquals(true, result);
    }
}
