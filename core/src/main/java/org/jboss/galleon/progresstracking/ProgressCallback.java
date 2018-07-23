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

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProgressCallback<T> {

    long DEFAULT_PROGRESS_PULSE_PCT = 20;
    long DEFAULT_PULSE_INTERVAL_MS = 1000;

    /**
     * Rate of the progress pulse as the percentage of the total volume
     * that is being processed. I.e if this method returns 10, it means
     * the pulse is expected every time a 10% of the total volume has been processed.
     *
     * If this method returns 0 then the pulse is expected per each processed item.
     *
     * If this method returns a negative value then the pulse is not expected at all.
     *
     * @return  rate of the progress pulse as the percentage of the total volume being processed
     */
    default long getProgressPulsePct() {
        return DEFAULT_PROGRESS_PULSE_PCT;
    }

    /**
     * Minimal progress pulse interval in milliseconds.
     *
     * If items are being processed very quickly and as the consequence the pulse appears to be too high,
     * this method allows to limit the pulse rate to a specific interval in milliseconds.
     *
     * If this method returns 0 or a negative value, the pulse will happen at the specified percentage
     * of total volume being processed.
     *
     * @return  minimal progress pulse interval in milliseconds
     */
    default long getMinPulseIntervalMs() {
        return DEFAULT_PULSE_INTERVAL_MS;
    }

    /**
     * Maximum progress pulse interval in milliseconds
     *
     * If items take too long to process and as the consequence the pulse appears to be too low,
     * this method allows to increase the pulse rate by specifying the maximum interval for it.
     * It is NOT a guaranteed maximum though. It only means that if the last pulse occurred longer ago
     * than the value returned by this method, the pulse will occur as soon as the current item has been processed.
     *
     * @return  maximum progress pulse interval in milliseconds
     */
    default long getMaxPulseIntervalMs() {
        return DEFAULT_PULSE_INTERVAL_MS;
    }

    /**
     * Called when the progress is about to begin
     *
     * @param tracker  progress tracker
     */
    void starting(ProgressTracker<T> tracker);

    /**
     * Called when a new item is about to be processed
     *
     * @param tracker  progress tracker
     */
    default void processing(ProgressTracker<T> tracker) {
    }

    /**
     * Called when the current item has been processed
     *
     * @param tracker  progress tracker
     */
    default void processed(ProgressTracker<T> tracker) {
    }

    /**
     * Progress pulse
     *
     * @param tracker  progress tracker
     */
    void pulse(ProgressTracker<T> tracker);

    /**
     * Called when all the items have been processed
     *
     * @param tracker  progress tracker
     */
    void complete(ProgressTracker<T> tracker);
}
