/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.progresstracking;

/**
 *
 * @author Alexey Loubyansky
 */
public interface ProgressTracker<T> {

    /**
     * The method is called when the processing is about to begin
     */
    void starting(long totalVolume);

    /**
     * This method indicates that an item is being processed
     */
    void processing(T item);

    /**
     * This method indicates that an item has been processed
     */
    void processed(T item);

    /**
     * Returns the current item
     *
     * @return  current item
     */
    T getItem();

    /**
     * The method is called after all the units have been processed
     */
    void complete();

    /**
     * Total number of units being processed
     *
     * @return  total number of units being processed
     */
    long getTotalVolume();

    /**
     * Returns the percentage of the units that have already been processed
     *
     * @return  percentage of the units that have already been processed
     */
    double getProgress();

    /**
     * Returns total number of units that have already been processed
     *
     * @return  total number of units that have already been processed
     */
    long getProcessedVolume();

    /**
     * Time interval in milliseconds since the last progress callback
     *
     * @return  time interval in milliseconds since the last progress callback
     */
    long getLastPulseInterval();
}
