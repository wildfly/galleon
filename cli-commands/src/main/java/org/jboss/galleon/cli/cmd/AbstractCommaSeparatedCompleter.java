/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.PmCompleterInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractCommaSeparatedCompleter extends AbstractCompleter {

    @Override
    public void complete(PmCompleterInvocation completerInvocation) {
        List<String> candidates = new ArrayList<>();
        List<String> all = getItems(completerInvocation);
        if (all.isEmpty()) {
            return;
        }
        String buffer = completerInvocation.getGivenCompleteValue();
        candidates.addAll(all);
        if (!buffer.isEmpty()) {
            List<String> specified = Arrays.asList(buffer.split(",+"));

            if (buffer.charAt(buffer.length() - 1) != ',') {
                String chunk = specified.get(specified.size() - 1);
                boolean needsComma = candidates.contains(chunk);
                candidates.removeAll(specified);
                if (candidates.isEmpty()) {
                    candidates.add(chunk);
                    completerInvocation.setAppendSpace(true);
                    completerInvocation.setOffset(chunk.length());
                } else {
                    final Iterator<String> iterator = candidates.iterator();
                    boolean hasMore = false;
                    while (iterator.hasNext()) {
                        if (!iterator.next().startsWith(chunk)) {
                            hasMore = true;
                            iterator.remove();
                        }
                    }
                    if (needsComma) {
                        candidates.add(chunk + ",");
                    }
                    completerInvocation.setOffset(chunk.length());
                    completerInvocation.setAppendSpace(!hasMore);
                }
            } else {
                candidates.removeAll(specified);
                if (candidates.size() == 1) {
                    completerInvocation.setOffset(0);
                }
            }
        }
        completerInvocation.addAllCompleterValues(candidates);

    }
}
