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
package org.jboss.galleon.cli.tracking;

import java.nio.file.Path;
import java.util.List;
import org.jboss.galleon.cli.PmSession;
import static org.jboss.galleon.cli.tracking.ConfigsTracker.DELAYED_EXECUTION_MSG;
import org.jboss.galleon.layout.ProvisioningLayoutFactory;
import org.jboss.galleon.progresstracking.ProgressTracker;
import org.jboss.galleon.state.ProvisionedConfig;

/**
 * The List index 0 contains the String phase, index 1 the content.
 * @author jdenise@redhat.com
 */
public class ExtraConfigsTracker extends CliProgressTracker<List<Object>> {

    public ExtraConfigsTracker(PmSession session) {
        super(session, "Additional configurations:", "Additional configurations generated.");
    }

    @Override
    protected String processingContent(ProgressTracker<List<Object>> tracker) {
        String content = "Preparing...";
        String phase = (String) tracker.getItem().get(0);
        if(tracker.getItem().get(1) instanceof ProvisionedConfig) {
            content = "Generating " + ((ProvisionedConfig) tracker.getItem().get(1)).getName();
        } else if (tracker.getItem().get(1) instanceof Path) {
            content = "Installing config "+ ((Path) tracker.getItem().get(1)).getFileName();
        } else if (tracker.getItem().get(1) == null&& ProvisioningLayoutFactory.TRACK_CONFIGS.equals(phase)) {
            content = DELAYED_EXECUTION_MSG;
        }
        return content;
    }

    @Override
    protected String completeContent(ProgressTracker<List<Object>> tracker) {
        return "";
    }

}
