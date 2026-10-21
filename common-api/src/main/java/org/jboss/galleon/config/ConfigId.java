/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.config;

/**
 * @author Alexey Loubyansky
 *
 */
public class ConfigId {

    final String model;
    final String name;

    public ConfigId(String model, String name) {
        if(model == null && name == null) {
            throw new IllegalArgumentException("Anonymous configs aren't supported");
        }
        this.model = model;
        this.name = name;
    }

    public boolean isModelOnly() {
        return model != null && name == null;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(model != null) {
            buf.append("model=").append(model);
        }
        if(name != null) {
            if(model != null) {
                buf.append(' ');
            }
            buf.append("name=").append(name);
        }
        return buf.append(']').toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigId other = (ConfigId) obj;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
