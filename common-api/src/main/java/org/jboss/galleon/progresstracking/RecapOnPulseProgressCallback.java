/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.progresstracking;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class RecapOnPulseProgressCallback<T> implements ProgressCallback<T> {

    protected final List<T> processedItems = new ArrayList<>();
    protected int processedVolume;

    @Override
    public void starting(ProgressTracker<T> tracker) {
        processedItems.clear();
        processedVolume = 0;
        doStart(tracker);
    }

    protected abstract void doStart(ProgressTracker<T> tracker);

    @Override
    public void processed(ProgressTracker<T> tracker) {
        processedItems.add(tracker.getItem());
    }

    @Override
    public void pulse(ProgressTracker<T> tracker) {
        for (int i = processedVolume; i < tracker.getProcessedVolume(); ++i) {
            recap(tracker, i + 1, processedItems.get(i));
        }
        processedVolume = (int) tracker.getProcessedVolume();
        doPulse(tracker);
    }

    protected void doPulse(ProgressTracker<T> tracker) {
    }

    protected abstract void recap(ProgressTracker<T> tracker, int index, T item);

    @Override
    public void complete(ProgressTracker<T> tracker) {
        if(processedVolume > 0 && processedVolume < tracker.getProcessedVolume()) {
            while (processedVolume < tracker.getProcessedVolume()) {
                recap(tracker, processedVolume + 1, processedItems.get(processedVolume++));
            }
        }
        processedItems.clear();
        processedVolume = 0;
        doComplete(tracker);
    }

    protected abstract void doComplete(ProgressTracker<T> tracker);
}
