/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
