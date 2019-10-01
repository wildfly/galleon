/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningOption {

    public static final ProvisioningOption OPTIONAL_PACKAGES = ProvisioningOption.builder(Constants.OPTIONAL_PACKAGES)
            .setDefaultValue(Constants.ALL)
            .addToValueSet(Constants.ALL)
            .addToValueSet(Constants.NONE)
            .addToValueSet(Constants.PASSIVE)
            .addToValueSet(Constants.PASSIVE_PLUS)
            .build();

    public static final ProvisioningOption VERSION_CONVERGENCE = ProvisioningOption.builder(Constants.VERSION_CONVERGENCE)
            .setDefaultValue(Constants.FIRST_PROCESSED)
            .addToValueSet(Constants.FIRST_PROCESSED)
            .addToValueSet(Constants.FAIL)
            .build();

    private static final List<ProvisioningOption> stdOptions = Arrays.asList(new ProvisioningOption[] {OPTIONAL_PACKAGES, VERSION_CONVERGENCE});

    public static List<ProvisioningOption> getStandardList() {
        return stdOptions;
    }

    private static Set<String> valueSetBoolean;

    public static Set<String> getBooleanValueSet() {
        return valueSetBoolean == null
                ? valueSetBoolean = Collections
                        .unmodifiableSet(new HashSet<>(Arrays.asList(new String[] { Constants.TRUE, Constants.FALSE })))
                : valueSetBoolean;
    }

    public static class Builder {

        private final String name;
        private boolean required;
        private boolean persistent = true;
        private String defaultValue;
        private Set<String> valueSet = Collections.emptySet();

        protected Builder(String name) {
            this.name = name;
        }

        public Builder setRequired() {
            this.required = true;
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setBooleanValueSet() {
            this.valueSet = getBooleanValueSet();
            return this;
        }

        public Builder addToValueSet(String... value) {
            if (value.length > 0) {
                for (String v : value) {
                    this.valueSet = CollectionUtils.add(valueSet, v);
                }
            }
            return this;
        }

        public Builder setPersistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        public ProvisioningOption build() {
            return new ProvisioningOption(this);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final String name;
    private final boolean required;
    private final boolean persistent;
    private final String defaultValue;
    private final Set<String> valueSet;

    protected ProvisioningOption(String name) {
        this.name = name;
        required = false;
        persistent = true;
        defaultValue = null;
        valueSet = getBooleanValueSet();
    }

    protected ProvisioningOption(Builder builder) {
        this.name = builder.name;
        this.required = builder.required;
        this.persistent = builder.persistent;
        this.defaultValue = builder.defaultValue;
        this.valueSet = CollectionUtils.unmodifiable(builder.valueSet);
        if (!valueSet.isEmpty() && defaultValue != null && !valueSet.contains(defaultValue)) {
            throw new IllegalArgumentException("The default value " + defaultValue + " of provisioning option " + name
                    + " is not in the allowed value set " + builder.valueSet);
        }
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

    /**
     * Indicates whether the option should be persisted in the provisioning configuration.
     *
     * @return  true if the option should be persisted, otherwise - false
     */
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (persistent ? 1231 : 1237);
        result = prime * result + (required ? 1231 : 1237);
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
        ProvisioningOption other = (ProvisioningOption) obj;
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
        if (persistent != other.persistent)
            return false;
        if (required != other.required)
            return false;
        if (valueSet == null) {
            if (other.valueSet != null)
                return false;
        } else if (!valueSet.equals(other.valueSet))
            return false;
        return true;
    }
}
