/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
