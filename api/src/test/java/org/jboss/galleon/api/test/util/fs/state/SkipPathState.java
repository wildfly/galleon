/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.api.test.util.fs.state;

import java.nio.file.Path;

/**
 *
 * @author Alexey Loubyansky
 */
public class SkipPathState extends PathState {

    public static class SkipPathBuilder extends PathState.Builder {

        private SkipPathBuilder(String name) {
            super(name);
        }

        public SkipPathState build() {
            return new SkipPathState(name);
        }
    }

    public static SkipPathBuilder builder(String name) {
        return new SkipPathBuilder(name);
    }

    protected SkipPathState(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.test.util.pathstate.PathState#doAssertState(java.nio.file.Path)
     */
    @Override
    protected void doAssertState(Path path) {
    }
}
