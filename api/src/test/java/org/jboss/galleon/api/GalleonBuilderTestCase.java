/*
 * Copyright 2016-2025 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.galleon.CoreVersion;
import org.jboss.galleon.DefaultMessageWriter;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.api.test.FeaturePackRepoTestBase;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author jdenise
 */
public class GalleonBuilderTestCase extends FeaturePackRepoTestBase {

    private static final FeaturePackLocation.FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private final MessageWriter log = new DefaultMessageWriter();

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
        final FeaturePackCreator fpCreator = initCreator();
        createFeaturePacks(fpCreator);
        fpCreator.install();
    }

    @Test
    public void test() throws Exception {

        GalleonBuilder builder = new GalleonBuilder();
        builder.addArtifactResolver(repo);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 0);

        // verify that releasing unused CL is logged
        AtomicReference<String> msg = new AtomicReference<>(null);
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), new DefaultMessageWriter() {
            @Override
            public void verbose(CharSequence message) {
                msg.set(message.toString());
            }
        });
        assertEquals("Releasing usage of core " + CoreVersion.getVersion() + " although no usage",
                msg.get());

        URLClassLoader l1 = GalleonBuilder.getCallerClassLoader(APIVersion.getVersion(), null);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 1);
        URLClassLoader l2 = GalleonBuilder.getCallerClassLoader(APIVersion.getVersion(), null);
        assertEquals(l1, l2);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 1);
        URLClassLoader l3 = GalleonBuilder.getCallerClassLoader(CoreVersion.getVersion(), null);
        assertEquals(l1, l3);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 1);
        // Release all usages.
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 0);
        URLClassLoader l4 = GalleonBuilder.getCallerClassLoader(CoreVersion.getVersion(), null);
        assertNotEquals(l1, l4);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 1);
        URLClassLoader loader = builder.getCoreClassLoader(CoreVersion.getVersion());
        assertNotNull(loader);
        URLClassLoader loader2 = builder.getCoreClassLoader(APIVersion.getVersion());
        assertNotNull(loader2);
        assertEquals(loader, loader2);
        assertEquals(GalleonBuilder.getClassLoaders().size(), 1);
        // Release all usages
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        GalleonBuilder.releaseUsage(CoreVersion.getVersion(), this.log);
        try {
            builder.getCoreClassLoader("foo");
            throw new Exception();
        } catch (ProvisioningException ex) {
            // XXX Expected.
        }
        assertEquals(APIVersion.getVersion(), builder.getCoreVersion(FP1_100_GAV.getLocation()));
    }

    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
                .newFeaturePack(FP1_100_GAV);
    }
}
