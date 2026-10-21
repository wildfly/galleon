/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.progresstracking;

/**
 *
 * @author Alexey Loubyansky
 */
public class NoOpProgressCallback<T> implements ProgressCallback<T> {

    @Override
    public long getProgressPulsePct() {
        return -1;
    }

    public void starting(ProgressTracker<T> tracker) {
    }

    public void processing(ProgressTracker<T> tracker) {
    }

    public void pulse(ProgressTracker<T> tracker) {
    }

    public void complete(ProgressTracker<T> tracker) {
    }
}
