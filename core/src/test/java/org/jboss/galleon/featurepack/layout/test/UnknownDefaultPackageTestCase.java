/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.featurepack.layout.test;



import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnknownDefaultPackageTestCase  {

    @Test
    public void testMain() throws Exception {
        final FPID fp1Gav = LegacyGalleon1Universe.newFPID("org.pm.test:fp-install", "1", "1.0.0.Beta1");
        try {
            FeaturePackLayout
                    .builder(FeaturePackSpec.builder(fp1Gav)
                            .addDefaultPackage("default"))
                    .addPackage(PackageSpec.forName("a"))
                    .build();
            Assert.fail("Non-existing default package");
        } catch(ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownPackage(fp1Gav, "default"), e.getMessage());
        }
    }
}
