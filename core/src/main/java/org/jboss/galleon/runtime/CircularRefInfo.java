/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.runtime;

import java.util.Comparator;

/**
 *
 * @author Alexey Loubyansky
 */
class CircularRefInfo {

    private static Comparator<CircularRefInfo> firstInConfigComparator;
    private static Comparator<CircularRefInfo> nextOnPathComparator;

    static Comparator<CircularRefInfo> getFirstInConfigComparator() {
        if(firstInConfigComparator == null) {
            firstInConfigComparator = new Comparator<CircularRefInfo>(){
                @Override
                public int compare(CircularRefInfo o1, CircularRefInfo o2) {
                    return o1.firstInConfig.includeNo - o2.firstInConfig.includeNo;
                }};
        }
        return firstInConfigComparator;
    }

    static Comparator<CircularRefInfo> getNextOnPathComparator() {
        if(nextOnPathComparator == null) {
            nextOnPathComparator = new Comparator<CircularRefInfo>(){
                @Override
                public int compare(CircularRefInfo o1, CircularRefInfo o2) {
                    return o1.nextOnPath.includeNo - o2.nextOnPath.includeNo;
                }};
        }
        return nextOnPathComparator;
    }


    final ResolvedFeature loopedOn;
    ResolvedFeature firstInConfig;
    ResolvedFeature nextOnPath;

    CircularRefInfo(ResolvedFeature start) {
        loopedOn = start;
        firstInConfig = start;
        nextOnPath = start;
    }

    void setNext(ResolvedFeature feature) {
        nextOnPath = feature;
        if(firstInConfig.includeNo > feature.includeNo) {
            firstInConfig = feature;
        }
    }
}
