/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.creator.tasks;

import java.io.IOException;

/**
 *
 * @author Alexey Loubyansky
 */
interface FsTask {

    void execute(FsTaskContext ctx) throws IOException;
    boolean isContent();
}
