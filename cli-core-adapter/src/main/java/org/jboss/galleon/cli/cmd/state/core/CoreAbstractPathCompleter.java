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
package org.jboss.galleon.cli.cmd.state.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.galleon.cli.CliLogging;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.core.GalleonCoreCompleter;
import org.jboss.galleon.cli.core.ProvisioningSession;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathParser;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class CoreAbstractPathCompleter implements GalleonCoreCompleter<ProvisioningSession> {

    @Override
    public void complete(PmCompleterInvocation completerInvocation, ProvisioningSession session) {
        List<String> items = getItems(completerInvocation, session);
        int i = completerInvocation.getGivenCompleteValue().lastIndexOf("" + PathParser.PATH_SEPARATOR) + 1;
        completerInvocation.setAppendSpace(false);
        completerInvocation.addAllCompleterValues(items);
        if (items.size() == 1) {
            if (completerInvocation.getGivenCompleteValue().endsWith(items.get(0))) {
                completerInvocation.setAppendSpace(true);
            }
        }
        completerInvocation.setOffset(completerInvocation.getGivenCompleteValue().length() - i);
    }

    List<String> getItems(PmCompleterInvocation completerInvocation, ProvisioningSession session) {
        try {
            FeatureContainer container = getContainer(completerInvocation, session);
            if (container == null) {
                return Collections.emptyList();
            }
            if (session.getCurrentPath() == null && completerInvocation.getGivenCompleteValue().isEmpty()) {
                return Arrays.asList("" + PathParser.PATH_SEPARATOR);
            }
            String buffer = completerInvocation.getGivenCompleteValue();
            if (!buffer.startsWith("" + PathParser.PATH_SEPARATOR)) {
                String currentPath = getCurrentPath(completerInvocation, session);
                if (currentPath != null) {
                    boolean completePath = currentPath.endsWith("" + PathParser.PATH_SEPARATOR);
                    buffer = currentPath + (completePath ? "" : "" + PathParser.PATH_SEPARATOR) + buffer;
                }
            }
            FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(container, true);
            PathParser.parse(buffer, consumer);
            List<String> candidates = consumer.getCandidates(buffer);
            filterCandidates(consumer, candidates);
            return candidates;
        } catch (Exception ex) {
            CliLogging.log.errorf("Exception while completing: {0}", ex.getLocalizedMessage());
        }
        return Collections.emptyList();
    }

    protected abstract String getCurrentPath(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception;

    protected abstract void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates);

    protected abstract FeatureContainer getContainer(PmCompleterInvocation completerInvocation, ProvisioningSession session) throws Exception;
}
