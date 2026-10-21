/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.diff.fs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.jboss.galleon.diff.FsDiff;

/**
 *
 * @author Alexey Loubyansky
 */
public class BasicFsDiffTestCase extends FsDiffTestBase {

    /* (non-Javadoc)
     * @see org.jboss.galleon.diff.fs.test.FsDiffTestBase#doInitOriginal()
     */
    @Override
    protected void initOriginalDir() throws IOException {
        createFile("a/b/c/d.txt", "d");
        createFile("a/b/originalOnlyFile.txt", "original");
        mkdir("a/b/originalOnlyDir/c/d");
        createFile("a/b/common.txt", "original");
        mkdir("originally_empty");
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.diff.fs.test.FsDiffTestBase#doInitOther()
     */
    @Override
    protected void initOtherDir() throws IOException {
        createFile("a/b/c/d.txt", "d");
        createFile("a/b/otherOnlyFile.txt", "other");
        mkdir("a/b/otherOnlyDir/c/d");
        createFile("a/b/common.txt", "other");
        createFile("originally_empty/other.txt", "other");
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.diff.fs.test.FsDiffTestBase#assertDiff(org.jboss.galleon.diff.FsDiff)
     */
    @Override
    protected void assertDiff(FsDiff diff) throws Exception {
        System.out.println(diff);
        assertFalse(diff.isEmpty());

        assertTrue(diff.hasRemovedEntries());
        final Set<String> removedPaths = diff.getRemovedPaths();
        assertEquals(2, removedPaths.size());
        assertTrue(removedPaths.contains("a/b/originalOnlyFile.txt"));
        assertTrue(removedPaths.contains("a/b/originalOnlyDir/"));

        assertTrue(diff.hasAddedEntries());
        final Set<String> addedPaths = diff.getAddedPaths();
        assertEquals(3, addedPaths.size());
        assertTrue(addedPaths.contains("a/b/otherOnlyFile.txt"));
        assertTrue(addedPaths.contains("a/b/otherOnlyDir/"));
        assertTrue(addedPaths.contains("originally_empty/other.txt"));

        assertTrue(diff.hasModifiedEntries());
        final Set<String> modifiedPaths = diff.getModifiedPaths();
        assertEquals(1, modifiedPaths.size());
        assertTrue(modifiedPaths.contains("a/b/common.txt"));
    }
}
