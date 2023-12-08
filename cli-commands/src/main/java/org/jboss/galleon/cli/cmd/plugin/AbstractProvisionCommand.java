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
package org.jboss.galleon.cli.cmd.plugin;

import org.jboss.galleon.cli.PmCommandActivator;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionCommand extends AbstractProvisionWithPlugins {

    public AbstractProvisionCommand(PmSession pmSession) {
        super(pmSession);
    }

    @Override
    protected String getName() {
        return "provision";
    }

    @Override
    protected PmCommandActivator getActivator() {
        return null;
    }
}
