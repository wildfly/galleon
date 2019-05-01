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
