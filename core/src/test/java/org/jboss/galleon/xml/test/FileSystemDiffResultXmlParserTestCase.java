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
package org.jboss.galleon.xml.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.diff.FileSystemDiffResult;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.xml.FileSystemDiffResultParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiffResultXmlParserTestCase {
     private static final XmlParserValidator<FileSystemDiffResult> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/pm-diff-result-1_0.xsd"), FileSystemDiffResultParser.getInstance());

    private static final Locale defaultLocale = Locale.getDefault();

    @BeforeClass
    public static void setLocale() {
        Locale.setDefault(Locale.US);
    }
    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void readValid() throws Exception {
        FileSystemDiffResult found = validator
                .validateAndParse("xml/diff/filesystem_changes.xml", null, null);
        assertThat(found.getAddedFiles().size(), is(2));
        assertThat(found.getDeletedFiles().size(), is(17));
        assertThat(found.getModifiedBinaryFiles().size(), is(2));
        assertThat(found.getUnifiedDiffs().size(), is(4));
    }

}
