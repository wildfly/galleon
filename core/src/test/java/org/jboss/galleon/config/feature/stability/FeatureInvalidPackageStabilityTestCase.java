/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.config.feature.stability;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningOption;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.state.ProvisionedState;

/**
 *
 * @author jfdenise
 */
public class FeatureInvalidPackageStabilityTestCase extends AbstractFeatureStabilityTestCase {

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningDescriptionException {
        return ProvisioningConfig.builder().
                addFeaturePackDep(FeaturePackConfig.builder(FP1_GAV.getLocation()).setInheritPackages(true).build()).
                addFeaturePackDep(FeaturePackConfig.builder(FP2_GAV.getLocation()).setInheritPackages(true).build()).
                addOption(Constants.PACKAGE_STABILITY_LEVEL, "default").
                addOption(Constants.STABILITY_LEVEL, "community").
                build();
    }

    @Override
    protected String[] pmErrors() throws ProvisioningException {
        String[] array = {ProvisioningOption.STABILITY_LEVEL.getName() + " option can't be set when "
                        + ProvisioningOption.PACKAGE_STABILITY_LEVEL.getName() + " is set."};
        return array;
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        throw new ProvisioningDescriptionException("Shouldn't be called.");
    }
}
