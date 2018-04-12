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
package org.jboss.galleon.cli.cmd.state.configuration;

import java.util.List;

import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.cmd.StateFullPathCompleter;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;

/**
 *
 * @author jdenise@redhat.com
 */
public class ProvisionedConfigurationCompleter extends StateFullPathCompleter {

    @Override
    protected String getCurrentPath(PmCompleterInvocation session) {
        return ConfigurationUtil.getCurrentPath(session.getPmSession());
    }

    @Override
    protected void filterCandidates(FeatureContainerPathConsumer consumer, List<String> candidates) {
        ConfigurationUtil.filterCandidates(consumer, candidates);
    }
}
