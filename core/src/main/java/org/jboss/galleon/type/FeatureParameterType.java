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
package org.jboss.galleon.type;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.runtime.CapabilityResolver;

/**
 * Represents a feature parameter type.
 *
 * @author Alexey Loubyansky
 */
public interface FeatureParameterType {

    /**
     * Unique type name.
     *
     * @return type name
     */
    String getName();

    /**
     * The default value for parameters of this type or null if there is no specific default value.
     *
     * @return default value
     */
    Object getDefaultValue();

    /**
     * Parses the string representation of a value of this type and creates the
     * corresponding Java object value.
     *
     * @param str  string representation of the value
     * @return  Java object value
     * @throws ParameterTypeConversionException  in case the parsing failed
     */
    Object fromString(String str) throws ParameterTypeConversionException;

    /**
     * Creates a string representation of the Java object value of this type.
     *
     * @param o  Java object value of the type
     * @return  string representation of the value
     * @throws ParameterTypeConversionException  in case the conversion failed
     */
    String toString(Object o) throws ParameterTypeConversionException;

    /**
     * Whether the value of the type is a collection value.
     *
     * @return true if the type represents a collection, otherwise false
     */
    boolean isCollection();

    /**
     * Whether the values of this type can be merged when merging feature configurations.
     *
     * @return  true if the values of the type can be merged, otherwise - false
     */
    boolean isMergeable();

    /**
     * Merges one value of the type into another.
     *
     * @param original  the value which appeared in the configuration first
     * @param other  the value which appeared in the configuration later
     * @return  the merged value
     * @throws ProvisioningException  in case merging failed
     */
    Object merge(Object original, Object other) throws ProvisioningException;

    /**
     * Resolves dynamic capability element from the value of the type and adds it
     * to the capability resolver.
     *
     * @param capResolver  capability resolver
     * @param o  value of the type
     * @return  true if the value was resolved and added, false otherwise
     * @throws ProvisioningException  in case the value resolution failed
     */
    boolean resolveCapabilityElement(CapabilityResolver capResolver, Object o) throws ProvisioningException;
}
