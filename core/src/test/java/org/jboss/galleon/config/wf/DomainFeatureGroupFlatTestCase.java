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
package org.jboss.galleon.config.wf;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class DomainFeatureGroupFlatTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addFeatureSpec(FeatureSpec.builder("extension")
                    .addParam(FeatureParameterSpec.createId("module"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("interface")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("inet-address", true))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("logger")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.createId("category"))
                    .addParam(FeatureParameterSpec.create("level", false))
                    .addFeatureRef(FeatureReferenceSpec.create("logging"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("logging")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.create("extension", "org.jboss.as.logging"))
                    .addFeatureRef(FeatureReferenceSpec.builder("extension").mapParam("extension", "module").build())
                    .addFeatureRef(FeatureReferenceSpec.create("profile"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("logging-console-handler")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.create("name", true, false, "CONSOLE"))
                    .addParam(FeatureParameterSpec.create("level", "INFO"))
                    .addParam(FeatureParameterSpec.create("formatters", "COLOR-PATTERN"))
                    .addFeatureRef(FeatureReferenceSpec.create("logging"))
                    .addFeatureRef(FeatureReferenceSpec.builder("logging-formatter").mapParam("profile", "profile").mapParam("formatters", "name").build())
                    .build())
            .addFeatureSpec(FeatureSpec.builder("logging-formatter")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("pattern"))
                    .addFeatureRef(FeatureReferenceSpec.builder("logging").mapParam("profile", "profile").build())
                    .build())
            .addFeatureSpec(FeatureSpec.builder("logging-rotating-file-handler")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.create("name", true, false, "FILE"))
                    .addParam(FeatureParameterSpec.create("level", "DEBUG"))
                    .addParam(FeatureParameterSpec.create("formatters", "PATTERN"))
                    .addParam(FeatureParameterSpec.create("relative-to", "jboss.server.log.dir"))
                    .addParam(FeatureParameterSpec.create("path", "server.log"))
                    .addParam(FeatureParameterSpec.create("suffix", ".yyyy-MM-dd"))
                    .addParam(FeatureParameterSpec.create("append", "true"))
                    .addParam(FeatureParameterSpec.create("autoflush", "true"))
                    .addFeatureRef(FeatureReferenceSpec.builder("logging").mapParam("profile", "profile").build())
                    .addFeatureRef(FeatureReferenceSpec.builder("logging-formatter").mapParam("profile", "profile").mapParam("formatters", "name").build())
                    .build())
            .addFeatureSpec(FeatureSpec.builder("profile")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("root-logger")
                    .addParam(FeatureParameterSpec.createId("profile"))
                    .addParam(FeatureParameterSpec.create("level", "INFO"))
                    .addParam(FeatureParameterSpec.create("console-handler", false, true, "CONSOLE"))
                    .addParam(FeatureParameterSpec.create("periodic-rotating-file-handler", false, true, "FILE"))
                    .addFeatureRef(FeatureReferenceSpec.builder("logging").mapParam("profile", "profile").build())
                    .addFeatureRef(FeatureReferenceSpec.builder("logging-console-handler").mapParam("profile", "profile").mapParam("console-handler", "name").build())
                    .addFeatureRef(FeatureReferenceSpec.builder("logging-rotating-file-handler").mapParam("profile", "profile").mapParam("periodic-rotating-file-handler", "name").build())
                    .build())
            .addFeatureSpec(FeatureSpec.builder("server-group")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("profile", false))
                    .addParam(FeatureParameterSpec.create("socket-binding-group", false))
                    .addFeatureRef(FeatureReferenceSpec.builder("socket-binding-group").mapParam("socket-binding-group", "name").build())
                    .addFeatureRef(FeatureReferenceSpec.create("profile"))
                    .build())
            .addFeatureSpec(FeatureSpec.builder("socket-binding")
                    .addParam(FeatureParameterSpec.createId("socket-binding-group"))
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("interface", true))
                    .addFeatureRef(FeatureReferenceSpec.builder("socket-binding-group").mapParam("socket-binding-group", "name").build())
                    .addFeatureRef(FeatureReferenceSpec.builder("interface").mapParam("interface", "name").setNillable(true).build())
                    .build())
            .addFeatureSpec(FeatureSpec.builder("socket-binding-group")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("default-interface", false))
                    .addFeatureRef(FeatureReferenceSpec.builder("interface").mapParam("default-interface", "name").build())
                    .build())
            .addFeatureGroup(FeatureGroup.builder("domain")
                    .addFeature(
                            new FeatureConfig("extension")
                            .setParam("module", "org.jboss.as.logging"))
                    .addFeature(
                            new FeatureConfig("profile")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("logging")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("logging-console-handler")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("logging-rotating-file-handler")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("logger")
                            .setParam("profile", "default")
                            .setParam("category", "com.arjuna")
                            .setParam("level", "WARN"))
                    .addFeature(
                            new FeatureConfig("logger")
                            .setParam("profile", "default")
                            .setParam("category", "org.jboss.as.config")
                            .setParam("level", "DEBUG"))
                    .addFeature(
                            new FeatureConfig("logger")
                            .setParam("profile", "default")
                            .setParam("category", "sun.rmi")
                            .setParam("level", "WARN"))
                    .addFeature(
                            new FeatureConfig("root-logger")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("logging-formatter")
                            .setParam("profile", "default")
                            .setParam("name", "PATTERN")
                            .setParam("pattern", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n"))
                    .addFeature(
                            new FeatureConfig("logging-formatter")
                            .setParam("profile", "default")
                            .setParam("name", "COLOR-PATTERN")
                            .setParam("pattern", "%K{level}%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n"))
                    .addFeature(
                            new FeatureConfig("profile")
                            .setParam("profile", "ha"))
                    .addFeature(
                            new FeatureConfig("logging")
                            .setParam("profile", "ha"))
                    .addFeature(
                            new FeatureConfig("logger")
                            .setParam("profile", "ha")
                            .setParam("category", "org.jboss.pm")
                            .setParam("level", "DEBUG"))
                    .addFeature(
                            new FeatureConfig("logger")
                            .setParam("profile", "ha")
                            .setParam("category", "java.util")
                            .setParam("level", "INFO"))
                    .addFeature(
                            new FeatureConfig("interface")
                            .setParam("name", "public"))
                    .addFeature(
                            new FeatureConfig("socket-binding-group")
                            .setParam("name", "standard-sockets")
                            .setParam("default-interface", "public"))
                    .addFeature(
                            new FeatureConfig("socket-binding")
                            .setParam("name", "http")
                            .setParam("socket-binding-group", "standard-sockets"))
                    .addFeature(
                            new FeatureConfig("socket-binding")
                            .setParam("name", "https")
                            .setParam("socket-binding-group", "standard-sockets"))
                    .addFeature(
                            new FeatureConfig("socket-binding-group")
                            .setParam("name", "ha-sockets")
                            .setParam("default-interface", "public"))
                    .addFeature(
                            new FeatureConfig("socket-binding")
                            .setParam("name", "http")
                            .setParam("socket-binding-group", "ha-sockets"))
                    .addFeature(
                            new FeatureConfig("socket-binding")
                            .setParam("name", "https")
                            .setParam("socket-binding-group", "ha-sockets"))
                    .addFeature(
                            new FeatureConfig("server-group")
                            .setParam("name", "main-server-group")
                            .setParam("socket-binding-group", "standard-sockets")
                            .setParam("profile", "default"))
                    .addFeature(
                            new FeatureConfig("server-group")
                            .setParam("name", "other-server-group")
                            .setParam("socket-binding-group", "ha-sockets")
                            .setParam("profile", "ha"))
                    .build())
            .addConfig(ConfigModel.builder().setName("main")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty(ConfigModel.BRANCH_PER_SPEC, "true")
                    .addFeatureGroup(FeatureGroup.forGroup("domain"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder().setName("main")
                        .setProperty("prop1", "value1")
                        .setProperty("prop2", "value2")
                        .setProperty(ConfigModel.BRANCH_PER_SPEC, "true")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "extension", "module", "org.jboss.as.logging")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "profile", "profile", "default")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "profile", "profile", "ha")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "logging", "profile", "default"))
                                .setConfigParam("extension", "org.jboss.as.logging")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "logging", "profile", "ha"))
                                .setConfigParam("extension", "org.jboss.as.logging")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logging-formatter")
                                .setParam("profile", "default")
                                .setParam("name", "PATTERN").build())
                                .setConfigParam("pattern", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logging-formatter")
                                .setParam("profile", "default")
                                .setParam("name", "COLOR-PATTERN").build())
                                .setConfigParam("pattern", "%K{level}%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logging-console-handler")
                                .setParam("profile", "default")
                                .setParam("name", "CONSOLE").build())
                                .setConfigParam("level", "INFO")
                                .setConfigParam("formatters", "COLOR-PATTERN")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logging-rotating-file-handler")
                                .setParam("profile", "default")
                                .setParam("name", "FILE").build())
                                .setConfigParam("level", "DEBUG")
                                .setConfigParam("formatters", "PATTERN")
                                .setConfigParam("relative-to", "jboss.server.log.dir")
                                .setConfigParam("path", "server.log")
                                .setConfigParam("suffix", ".yyyy-MM-dd")
                                .setConfigParam("append", "true")
                                .setConfigParam("autoflush", "true")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logger")
                                .setParam("profile", "default")
                                .setParam("category", "com.arjuna").build())
                                .setConfigParam("level", "WARN")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logger")
                                .setParam("profile", "default")
                                .setParam("category", "org.jboss.as.config").build())
                                .setConfigParam("level", "DEBUG")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logger")
                                .setParam("profile", "default")
                                .setParam("category", "sun.rmi").build())
                                .setConfigParam("level", "WARN")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logger")
                                .setParam("profile", "ha")
                                .setParam("category", "org.jboss.pm").build())
                                .setConfigParam("level", "DEBUG")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "logger")
                                .setParam("profile", "ha")
                                .setParam("category", "java.util").build())
                                .setConfigParam("level", "INFO")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "root-logger", "profile", "default"))
                                .setConfigParam("level", "INFO")
                                .setConfigParam("console-handler", "CONSOLE")
                                .setConfigParam("periodic-rotating-file-handler", "FILE")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "interface", "name", "public")).build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "socket-binding-group", "name", "standard-sockets"))
                                .setConfigParam("default-interface", "public")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "socket-binding-group", "name", "ha-sockets"))
                                .setConfigParam("default-interface", "public")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "socket-binding")
                                .setParam("socket-binding-group", "standard-sockets")
                                .setParam("name", "http")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "socket-binding")
                                .setParam("socket-binding-group", "standard-sockets")
                                .setParam("name", "https")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "socket-binding")
                                .setParam("socket-binding-group", "ha-sockets")
                                .setParam("name", "http")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(
                                ResolvedFeatureId.builder(FP_GAV.getProducer(), "socket-binding")
                                .setParam("socket-binding-group", "ha-sockets")
                                .setParam("name", "https")
                                .build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "server-group", "name", "main-server-group"))
                                .setConfigParam("socket-binding-group", "standard-sockets")
                                .setConfigParam("profile", "default")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV.getProducer(), "server-group", "name", "other-server-group"))
                                .setConfigParam("socket-binding-group", "ha-sockets")
                                .setConfigParam("profile", "ha")
                                .build())
                        .build())
                .build();
    }
}
