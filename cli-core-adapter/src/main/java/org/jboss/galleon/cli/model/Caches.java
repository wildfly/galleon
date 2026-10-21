/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 *
 * @author jdenise@redhat.com
 */
public class Caches {

    private static final Map<FPID, FeatureContainer> FP_CACHE = new HashMap<>();

    public static FeatureContainer getFeaturePackInfo(FPID fpid) {
        return FP_CACHE.get(fpid);
    }

    public static void addFeaturePackInfo(FPID fpid, FeatureContainer info) {
        FP_CACHE.put(fpid, info);
    }

}
