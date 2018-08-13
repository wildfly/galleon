/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
