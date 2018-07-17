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
public class ProvisioningProgressTracker {

    private final ProvisioningProgressCallback callback;
    private final int requestedProgressPulse;
    private int totalVolume;
    private int pulseVolume;
    private long actualPulse;

    private int accumulatedPulseVolume;
    private int accumulatedVolume;

    public ProvisioningProgressTracker(ProvisioningProgressCallback callback, int requestedProgressPulse) {
        if(requestedProgressPulse > 100 || requestedProgressPulse < 1) {
            throw new IllegalArgumentException("The argument expected to be in the range from 1 to 100 but was " + requestedProgressPulse);
        }
        this.callback = callback;
        this.requestedProgressPulse = requestedProgressPulse;
    }

    public void setVolume(int volume) {
        this.totalVolume = volume;
        final double pulseVolume = (double) requestedProgressPulse * volume / 100;
        this.pulseVolume = (int) pulseVolume;
        if(pulseVolume != this.pulseVolume) {
            ++this.pulseVolume;
            actualPulse = Math.round(((double)100/ volume)*this.pulseVolume);
        } else {
            actualPulse = requestedProgressPulse;
        }
    }

    public void starting() {
        callback.starting(this);
    }

    public void progressed() {
        progressed(1);
    }

    public void progressed(int volume) {
        if(accumulatedVolume >= totalVolume) {
            return;
        }
        accumulatedPulseVolume += volume;
        while (accumulatedPulseVolume >= pulseVolume) {
            accumulatedPulseVolume -= pulseVolume;
            accumulatedVolume += pulseVolume;
            callback.progressed(this);
            if(accumulatedVolume >= totalVolume) {
                return;
            }
        }
    }

    public void complete() {
        callback.starting(this);
    }

    public double getAccumulatedVolume() {
        return (double)accumulatedVolume*100/totalVolume;
    }

    public static void main(String[] args) throws Exception {

        final int totalVolume = 50;
        final int requestedPulseRate = 3;

        ProvisioningProgressTracker tracker = new ProvisioningProgressTracker(new ProvisioningProgressCallback() {
            @Override
            public void starting(ProvisioningProgressTracker tracker) {
                System.out.println("starting");
            }
            @Override
            public void progressed(ProvisioningProgressTracker tracker) {
                System.out.println("progressed " + tracker.getAccumulatedVolume());
            }
            @Override
            public void complete(ProvisioningProgressTracker tracker) {
                System.out.println("complete!");
            }},
                requestedPulseRate);

        tracker.setVolume(totalVolume);

        System.out.println("total volume " + tracker.totalVolume);
        System.out.println("requested pulse " + tracker.requestedProgressPulse);
        System.out.println("actual pulse " + tracker.actualPulse);
        System.out.println("pulse volume " + tracker.pulseVolume);

        System.out.println("");
        //for(int i = 0; i < totalVolume; ++i) {
        //    tracker.progressed();
        //}
        tracker.progressed(7);
        tracker.progressed(7);
    }
}
