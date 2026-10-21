/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.galleon.api;

import java.nio.file.Path;
import java.util.Map;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.core.builder.LocalFP;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseResolver;

public class ProvisioningBuilder {

    private Path installationHome;
    private MessageWriter messageWriter;
    private final UniverseResolver resolver;
    private boolean logTime;
    private boolean recordState = true;
    private final String coreVersion;
    private final Map<FeaturePackLocation.FPID, LocalFP> locals;

    ProvisioningBuilder(UniverseResolver resolver, Map<FeaturePackLocation.FPID, LocalFP> locals, String coreVersion) throws ProvisioningException {
        this.resolver = resolver;
        this.locals = locals;
        this.coreVersion = coreVersion;
    }

    public ProvisioningBuilder setInstallationHome(Path installationHome) {
        this.installationHome = installationHome;
        return this;
    }

    public ProvisioningBuilder setMessageWriter(MessageWriter messageWriter) {
        this.messageWriter = messageWriter;
        return this;
    }

    public ProvisioningBuilder setLogTime(boolean logTime) {
        this.logTime = logTime;
        return this;
    }

    public ProvisioningBuilder setRecordState(boolean recordState) {
        this.recordState = recordState;
        return this;
    }

    public Provisioning build() throws ProvisioningException {

        return new ProvisioningImpl(this);
    }

    /**
     * Location of the installation.
     *
     * @return location of the installation
     */
    public Path getInstallationHome() {
        return installationHome;
    }

    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    /**
     * Whether to log provisioning time
     *
     * @return Whether provisioning time should be logged at the end
     */
    public boolean isLogTime() {
        return logTime;
    }

    public boolean isRecordState() {
        return recordState;
    }

    UniverseResolver getUniverseResolver() throws ProvisioningException {
        return resolver;
    }

    Map<FeaturePackLocation.FPID, LocalFP> getLocals() {
        return locals;
    }

    public String getGalleonCoreVersion() {
        return coreVersion;
    }

}
