/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.userchanges.persist.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.BaseErrors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.state.ProvisionedConfig;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.test.util.fs.state.DirState.DirBuilder;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.userchanges.test.UserChangesTestBase;
import org.jboss.galleon.xml.ProvisionedConfigXmlWriter;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PersistChangesTestBase extends UserChangesTestBase {

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        final ProvisionedState.Builder builder = ProvisionedState.builder();
        final FPID[] fpids = provisionedFpids();
        if(fpids != null && fpids.length > 0) {
            for(FPID fpid : fpids) {
                builder.addFeaturePack(ProvisionedFeaturePack.forFPID(fpid));
            }
            final ProvisionedConfig[] configs = provisionedConfigModels();
            if(configs != null && configs.length > 0) {
                for(ProvisionedConfig config : configs) {
                    builder.addConfig(config);
                }
            }
        }
        return builder.build();
    }

    protected abstract FPID[] provisionedFpids();

    protected ProvisionedConfig[] provisionedConfigModels() throws ProvisioningException {
        return null;
    }

    @Override
    protected DirState provisionedHomeDir() {
        final DirBuilder builder = newDirBuilder();
        provisionedPackagesContent(builder);
        try {
            addConfigFiles(builder);
        } catch (ProvisioningException e) {
            throw new IllegalStateException(e);
        }
        return builder.build();
    }

    protected void provisionedPackagesContent(DirBuilder builder) {
    }

    private void addConfigFiles(DirBuilder builder) throws ProvisioningException {
        final ProvisionedConfig[] configs = provisionedConfigModels();
        if(configs == null || configs.length == 0) {
            return;
        }
        final StringBuilder buf = new StringBuilder();
        for (ProvisionedConfig config : configs) {
            buf.append(Constants.CONFIGS).append('/');
            if (config.getModel() != null) {
                buf.append(config.getModel()).append('/');
            }
            buf.append(config.getName());
            builder.addFile(buf.toString(), toXmlString(config));
            buf.setLength(0);
        }
    }

    protected void overwrite(ProvisionedConfig config) throws ProvisioningException {
        Path p = this.installHome.resolve(Constants.CONFIGS);
        if(config.getModel() != null) {
            p = p.resolve(config.getModel());
        }
        p = p.resolve(config.getName());
        try {
            Files.createDirectories(p.getParent());
        } catch (IOException e1) {
            throw new ProvisioningException(BaseErrors.mkdirs(p.getParent()), e1);
        }
        try(BufferedWriter writer = Files.newBufferedWriter(p)) {
            ProvisionedConfigXmlWriter.getInstance().write(config, writer);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException("Failed to store " + new ConfigId(config.getModel(), config.getName()) + " in a string", e);
        }
    }

    private static String toXmlString(ProvisionedConfig config) throws ProvisioningException {
        try(StringWriter writer = new StringWriter()) {
            ProvisionedConfigXmlWriter.getInstance().write(config, writer);
            return writer.getBuffer().toString();
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException("Failed to store " + new ConfigId(config.getModel(), config.getName()) + " in a string", e);
        }
    }
}
