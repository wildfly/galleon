/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
