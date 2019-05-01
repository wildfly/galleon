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
