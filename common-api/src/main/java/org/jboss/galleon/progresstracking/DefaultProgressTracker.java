/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Alexey Loubyansky
 */
public class DefaultProgressTracker<T> implements ProgressTracker<T> {

    private final ProgressCallback<T> callback;

    private long totalVolume;
    private long pulseAtVolume;
    private long pulsePct;
    private long minPulseInterval;
    private long maxPulseInterval;

    private long processedSincePulse;
    private long processedVolume;

    private long lastPulseNano;
    private long lastPulseInterval;
    private T item;

    public DefaultProgressTracker(ProgressCallback<T> callback) {
        this.callback = callback;
        this.pulsePct = callback.getProgressPulsePct();
        if(pulsePct > 100) {
            throw new IllegalArgumentException("The argument was not expected to be bigger than 100 but was " + pulsePct);
        }
        setMinPulseMs(callback.getMinPulseIntervalMs());
        setMaxPulseMs(callback.getMaxPulseIntervalMs());
    }

    private DefaultProgressTracker<T> setMinPulseMs(long minPulseIntervalMs) {
        this.minPulseInterval = minPulseIntervalMs <= 0 ? minPulseIntervalMs : minPulseIntervalMs*1000000;
        return this;
    }

    private DefaultProgressTracker<T> setMaxPulseMs(long maxPulseIntervalMs) {
        this.maxPulseInterval = maxPulseIntervalMs <= 0 ? maxPulseIntervalMs : maxPulseIntervalMs*1000000;
        return this;
    }

    public static <T> void process(DefaultProgressTracker<T> tracker, TrackedProcessor processor, Collection<T> col) throws Exception {
        tracker.starting(col.size());
        doProcess(tracker, processor, col);
    }

    public static <T> void process(DefaultProgressTracker<T> tracker, TrackedProcessor processor, T[] arr) throws Exception {
        tracker.starting(arr.length);
        doProcess(tracker, processor, Arrays.asList(arr));
    }

    public static <T> void process(DefaultProgressTracker<T> tracker, TrackedProcessor processor, Iterable<T> i) throws Exception {
        tracker.starting(-1);
        doProcess(tracker, processor, i);
    }

    private static <T> void doProcess(ProgressTracker<T> tracker, TrackedProcessor processor, Iterable<T> i) throws Exception {
        for(T o : i) {
            tracker.processing(o);
            processor.process(o);
            tracker.processed(o);
        }
        tracker.complete();
    }

    @Override
    public void starting(long totalVolume) {

        this.totalVolume = totalVolume;
        if(totalVolume >= 0 && pulsePct > 0) {
            final double pulseAtVolume = (double) pulsePct * totalVolume / 100;
            this.pulseAtVolume = (int) pulseAtVolume;
            if (pulseAtVolume != this.pulseAtVolume) {
                ++this.pulseAtVolume;
                pulsePct = Math.round(((double) 100 / totalVolume) * this.pulseAtVolume);
            }
        } else {
            pulseAtVolume = pulsePct < 0 ? pulsePct : 1;
        }

        processedSincePulse = 0;
        processedVolume = 0;
        callback.starting(this);
        lastPulseInterval = -1;
        lastPulseNano = System.nanoTime();
    }

    @Override
    public void processing(T item) {
        this.item = item;
        callback.processing(this);
    }

    @Override
    public void processed(T item) {
        if(pulseAtVolume < 0 || totalVolume >= 0 && processedVolume >= totalVolume) {
            return;
        }
        ++processedVolume;
        callback.processed(this);
        if (++processedSincePulse >= pulseAtVolume) {
            do {
                processedSincePulse -= pulseAtVolume;
                if (minPulseInterval <= 0) {
                    callback.pulse(this);
                } else {
                    final long curTime = System.nanoTime();
                    if (curTime - lastPulseNano >= minPulseInterval) {
                        lastPulseInterval = curTime - lastPulseNano;
                        callback.pulse(this);
                        lastPulseNano = curTime;
                    }
                }
            } while (processedSincePulse >= pulseAtVolume);
            return;
        }
        if(maxPulseInterval > 0) {
            final long curTime = System.nanoTime();
            if (curTime - lastPulseNano >= maxPulseInterval) {
                lastPulseInterval = curTime - lastPulseNano;
                callback.pulse(this);
                lastPulseNano = curTime;
                processedSincePulse = 0;
            }
        }
    }

    public T getItem() {
        return item;
    }

    @Override
    public void complete() {
        if(lastPulseInterval > 0 && processedSincePulse > 0) {
            callback.pulse(this);
        }
        callback.complete(this);
    }

    @Override
    public long getTotalVolume() {
        return totalVolume;
    }

    @Override
    public double getProgress() {
        return totalVolume < 0 ? 0 : (double)(processedVolume)*100/totalVolume;
    }

    @Override
    public long getProcessedVolume() {
        return processedVolume;
    }

    @Override
    public long getLastPulseInterval() {
        return lastPulseInterval < 0 ? lastPulseInterval : lastPulseInterval / 1000000;
    }

    public static void main(String[] args) throws Exception {

        DefaultProgressTracker<Long> tracker = new DefaultProgressTracker<>(new ProgressCallback<Long>() {
            @Override
            public long getProgressPulsePct() {
                return 10;
            }
            @Override
            public long getMinPulseIntervalMs() {
                return 500;
            }
            @Override
            public long getMaxPulseIntervalMs() {
                return 500;
            }
            @Override
            public void starting(ProgressTracker<Long> tracker) {
                System.out.println("Processing");
            }
            @Override
            public void processing(ProgressTracker<Long> tracker) {
            }
            @Override
            public void pulse(ProgressTracker<Long> tracker) {
                System.out.println(String.format("  %s of %s (%s%%), %sms", tracker.getProcessedVolume(), tracker.getTotalVolume(), Math.round(tracker.getProgress()), tracker.getLastPulseInterval()));
            }
            @Override
            public void complete(ProgressTracker<Long> tracker) {
                System.out.println("  Complete!");
            }});

        final long totalVolume = 50;
        trackVolume(totalVolume, tracker);
        trackFlow(totalVolume, tracker);
    }

    private static void trackVolume(final long totalVolume, DefaultProgressTracker<Long> tracker) throws InterruptedException {

        System.out.println();
        System.out.println("total volume " + tracker.totalVolume);
        System.out.println("actual pulse " + tracker.pulsePct);
        System.out.println("pulse volume " + tracker.pulseAtVolume);
        System.out.println("min pulse ms " + tracker.minPulseInterval/1000000);

        System.out.println();
        //for(int i = 0; i < totalVolume; ++i) {
        //    tracker.progressed();
        //}
        tracker.starting(totalVolume);
        int i = 0;
        while(true) {
            tracker.processing((long)i);
            tracker.processed((long)i);
            if(++i >= totalVolume) {
                break;
            }
            Thread.sleep(500 - i*10);
        }
        tracker.complete();
    }

    private static void trackFlow(final long totalVolume, DefaultProgressTracker<Long> tracker) throws InterruptedException {

        System.out.println();
        System.out.println("total volume " + tracker.totalVolume);
        System.out.println("actual pulse " + tracker.pulsePct);
        System.out.println("pulse volume " + tracker.pulseAtVolume);
        System.out.println("min pulse ms " + tracker.minPulseInterval/1000000);

        System.out.println("");
        //for(int i = 0; i < totalVolume; ++i) {
        //    tracker.progressed();
        //}
        tracker.starting(totalVolume);
        int i = 0;
        while(true) {
            tracker.processing((long)i);
            tracker.processed((long)i);
            if(++i >= totalVolume) {
                break;
            }
            Thread.sleep(500 - i*10);
        }
        tracker.complete();
    }
}
