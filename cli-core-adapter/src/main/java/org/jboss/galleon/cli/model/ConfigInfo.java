/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli.model;

import java.util.Collection;
import java.util.Objects;
import org.jboss.galleon.config.ConfigId;

/**
 *
 * @author jdenise@redhat.com
 */
public class ConfigInfo {

    private final String model;
    private final String name;
    private Group root;
    private final ConfigId id;
    private final Collection<ConfigId> layers;

    public ConfigInfo(String model, String name, Collection<ConfigId> layers) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(name);
        this.model = model;
        this.name = name;
        this.id = new ConfigId(model, name);
        this.layers = layers;
    }

    public Collection<ConfigId> getlayers() {
        return layers;
    }

    public ConfigId getId() {
        return id;
    }

    public void setFeatureGroupRoot(Group group) {
        root = group;
    }

    public Group getRoot() {
        return root;
    }
    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ConfigInfo)) {
            return false;
        }
        ConfigInfo ci = (ConfigInfo) other;
        return name.equals(ci.name) && model.equals(ci.model);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.model);
        hash = 73 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
