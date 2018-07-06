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
package org.jboss.galleon.test;

import java.util.Arrays;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.test.util.fs.state.DirState.DirBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmTestBase extends FeaturePackRepoTestBase {

    private ProvisioningConfig initialProvisioningConfig;
    private ProvisionedState initialProvisionedState;
    private DirState initialHomeDirState;

    protected abstract void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException;

    protected ProvisioningConfig initialState() throws ProvisioningException {
        return null;
    }

    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return initialProvisioningConfig;
    }

    protected ProvisionedState provisionedState() throws ProvisioningException {
        return initialProvisionedState;
    }

    protected DirState provisionedHomeDir() {
        return null;
    }

    protected boolean assertProvisionedHomeDir() {
        return true;
    }

    protected abstract void testPm(ProvisioningManager pm) throws ProvisioningException;

    @Override
    protected void doBefore() throws Exception {
        super.doBefore();
        createFeaturePacks(initCreator());
        initialProvisioningConfig = initialState();
        if(initialProvisioningConfig != null) {
            try (ProvisioningManager pm = getPm()) {
                pm.provision(initialProvisioningConfig);
                initialProvisionedState = pm.getProvisionedState();
            }
        }
        initialHomeDirState = DirState.rootBuilder().init(installHome).build();
    }

    protected DirBuilder newDirBuilder() {
        return DirState.rootBuilder().skip(Constants.PROVISIONED_STATE_DIR);
    }

    protected String[] pmErrors() throws ProvisioningException {
        return null;
    }

    @Test
    public void main() throws Throwable {
        final String[] errors = pmErrors();
        boolean failed = false;
        ProvisioningManager pm = getPm();
        try {
            testPm(pm);
            pmSuccess();
            if(errors != null) {
                Assert.fail("Expected failures: " + Arrays.asList(errors));
            }
            assertProvisionedConfig(pm);
            assertProvisionedState(pm);
        } catch(AssertionError e) {
            throw e;
        } catch(Throwable t) {
            failed = true;
            if (errors == null) {
                pmFailure(t);
            } else {
                assertErrors(t, errors);
            }
            assertProvisioningConfig(pm, initialProvisioningConfig);
            assertProvisionedState(pm, initialProvisionedState);
        } finally {
            pm.close();
        }

        DirState expectedHomeDir = provisionedHomeDir();
        if(expectedHomeDir == null) {
            if(!assertProvisionedHomeDir()) {
                return;
            }
            if(failed || initialProvisioningConfig != null) {
                expectedHomeDir = initialHomeDirState;
            } else {
                expectedHomeDir = DirState.rootBuilder().skip(Constants.PROVISIONED_STATE_DIR).build();
            }
        }
        expectedHomeDir.assertState(installHome);
    }

    protected void pmSuccess() {
    }


    protected void pmFailure(Throwable t) throws Throwable {
        throw t;
    }

    protected void assertProvisionedState(final ProvisioningManager pm) throws ProvisioningException {
        assertProvisionedState(pm, provisionedState());
    }

    protected void assertProvisionedConfig(final ProvisioningManager pm) throws ProvisioningException {
        assertProvisioningConfig(pm, provisionedConfig());
    }

    protected void assertErrors(Throwable t, String... msgs) {
        int i = 0;
        if(msgs != null) {
            while (t != null && i < msgs.length) {
                Assert.assertEquals(msgs[i++], t.getLocalizedMessage());
                t = t.getCause();
            }
        }
        if(t != null) {
            Assert.fail("Unexpected error: " + t.getLocalizedMessage());
        }
        if(i < msgs.length - 1) {
            Assert.fail("Not reported error: " + msgs[i]);
        }
    }
}
