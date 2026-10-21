/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aesh.command.completer.OptionCompleter;

/**
 * Base class for completion of discrete values.
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractCompleter implements OptionCompleter<PmCompleterInvocation> {

    @Override
    public void complete(PmCompleterInvocation completerInvocation) {
        List<String> items = getItems(completerInvocation);
        if (items != null && !items.isEmpty()) {
            List<String> candidates = new ArrayList<>();
            String opBuffer = completerInvocation.getGivenCompleteValue();
            if (opBuffer.isEmpty()) {
                candidates.addAll(items);
            } else {
                for (String name : items) {
                    if (name.startsWith(opBuffer)) {
                        candidates.add(name);
                    }
                }
                Collections.sort(candidates);
            }
            completerInvocation.addAllCompleterValues(candidates);
        }
    }

    protected abstract List<String> getItems(PmCompleterInvocation completerInvocation);

}
