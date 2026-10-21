/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.progresstracking;

/**
 *
 * @author Alexey Loubyansky
 */
public interface TrackedProcessor {

    void process(Object o) throws Exception;
}
