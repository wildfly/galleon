/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.plugin;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;

/**
 * Provisioning plugin option description.
 *
 * @author Alexey Loubyansky
 */
public class PluginOption {

    public static class Builder {

        private final String name;
        private boolean required;
        private boolean acceptsValue = true;
        private String defaultValue;
        private Set<String> valueSet = Collections.emptySet();

        private Builder(String name) {
            this.name = name;
        }

        public Builder setRequired() {
            this.required = true;
            return this;
        }

        public Builder hasNoValue() {
            this.acceptsValue = false;
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder addToValueSet(String value) {
            this.valueSet = CollectionUtils.add(valueSet, value);
            return this;
        }

        public PluginOption build() {
            return new PluginOption(this);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static PluginOption forName(String name) {
        return new PluginOption(name);
    }

    private final String name;
    private final boolean required;
    private final boolean acceptsValue;
    private final String defaultValue;
    private final Set<String> valueSet;

    private PluginOption(String name) {
        this.name = name;
        required = false;
        acceptsValue = false;
        defaultValue = null;
        valueSet = Collections.emptySet();
    }

    private PluginOption(Builder builder) {
        this.name = builder.name;
        this.required = builder.required;
        this.acceptsValue = builder.acceptsValue;
        this.defaultValue = builder.defaultValue;
        this.valueSet = builder.valueSet;
    }

    /**
     * Option name, must be unique across plugins
     * @return  option name
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether the option must be set by the user for the plugin to function
     * @return  whether the option is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Indicates whether the option accepts a value or can appear w/o a value
     * @return  whether the option accepts a value
     */
    public boolean isAcceptsValue() {
        return acceptsValue;
    }

    /**
     * The default value for the option in case the option was not explicitly set
     * by the user. May be null.
     * @return  default option value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Allowed option values. Empty value set indicates there are no restrictions
     * on the option value.
     * @return  allowed option values or an empty set in case there are no restrictions on the value
     */
    public Set<String> getValueSet() {
        return valueSet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + (acceptsValue ? 1231 : 1237);
        result = prime * result + ((valueSet == null) ? 0 : valueSet.hashCode());
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
        PluginOption other = (PluginOption) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        } else if (!defaultValue.equals(other.defaultValue))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (required != other.required)
            return false;
        if (acceptsValue != other.acceptsValue)
            return false;
        if (valueSet == null) {
            if (other.valueSet != null)
                return false;
        } else if (!valueSet.equals(other.valueSet))
            return false;
        return true;
    }
}
