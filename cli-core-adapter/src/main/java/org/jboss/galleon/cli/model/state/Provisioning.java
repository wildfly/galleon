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
package org.jboss.galleon.cli.model.state;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.UniverseSpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class Provisioning {

    private class AddUniverseAction implements State.Action {

        private final String name;
        private final String factory;
        private final String location;

        AddUniverseAction(String name, String factory, String location) {
            this.name = name;
            this.factory = factory;
            this.location = location;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (name == null) {
                builder.setDefaultUniverse(factory, location);
            } else {
                builder.addUniverse(name, factory, location);
            }
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            builder.removeUniverse(name);
        }
    }

    private static class RemoveUniverseAction implements State.Action {

        private final String name;
        private UniverseSpec existing;

        RemoveUniverseAction(String name) {
            this.name = name;
        }

        @Override
        public void doAction(ProvisioningConfig current, ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (name == null) {
                existing = current.getDefaultUniverse();
            } else {
                existing = current.getUniverseSpec(name);
            }
            if (existing == null) {
                throw new ProvisioningException("No default Universe to remove");
            }
            builder.removeUniverse(name);
        }

        @Override
        public void undoAction(ProvisioningConfig.Builder builder) throws ProvisioningException {
            if (name == null) {
                builder.setDefaultUniverse(existing);
            } else {
                builder.addUniverse(name, existing);
            }
        }
    }

    State.Action addUniverse(String name, String factory, String location) {
        return new AddUniverseAction(name, factory, location);
    }

    State.Action removeUniverse(String name) {
        return new RemoveUniverseAction(name);
    }
}
