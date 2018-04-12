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
package org.jboss.galleon.cli.cmd.state;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aesh.utils.Config;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.PmCommandInvocation;
import org.jboss.galleon.cli.model.FeatureContainer;
import org.jboss.galleon.cli.model.FeatureInfo;
import org.jboss.galleon.cli.model.FeatureSpecInfo;
import org.jboss.galleon.cli.model.Group;
import org.jboss.galleon.cli.model.Identity;
import org.jboss.galleon.cli.model.PackageInfo;
import org.jboss.galleon.cli.path.FeatureContainerPathConsumer;
import org.jboss.galleon.cli.path.PathConsumerException;
import org.jboss.galleon.cli.path.PathParser;
import org.jboss.galleon.cli.path.PathParserException;
import org.jboss.galleon.spec.CapabilitySpec;
import org.jboss.galleon.spec.FeatureAnnotation;
import org.jboss.galleon.spec.FeatureDependencySpec;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.PackageDependencySpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class StateInfoUtil {

    public static void printContentPath(PmCommandInvocation session, FeatureContainer fp, String path)
            throws ProvisioningException, PathParserException, PathConsumerException, IOException {
        FeatureContainerPathConsumer consumer = new FeatureContainerPathConsumer(fp, false);
        PathParser.parse(path, consumer);
        Group grp = consumer.getCurrentNode(path);
        if (grp != null) { // entered some content
            if (grp.getFeature() != null) {
                displayFeature(session, grp);
            } else if (grp.getSpec() != null) {
                displayFeatureSpec(session, grp);
            } else if (grp.getPackage() != null) {
                PackageInfo pkg = grp.getPackage();
                if (pkg.isModule()) {
                    displayModule(session, grp);
                } else {
                    displayPackage(session, grp);
                }
            } else if (!grp.getGroups().isEmpty()) {
                displayContainmentGroup(session, grp);
            }
        }
    }

    private static void displayContainmentGroup(PmCommandInvocation session, Group grp) {
        for (Group fg : grp.getGroups()) {
            session.println(fg.getIdentity().getName());
        }
    }

    private static void displayFeature(PmCommandInvocation session, Group grp) throws ProvisioningException {
        // Feature and spec.
        FeatureInfo f = grp.getFeature();
        session.println("Name       : " + f.getName());
        session.println("Type       : " + f.getType());
        session.println("Origin     : " + f.getSpecId().getGav());
        session.println("Description: " + f.getDescription());
        session.println("Feature XML extract");
        StringBuilder xmlBuilder = new StringBuilder();
        /**
         * <feature spec="core-service.vault">
         * <param name="core-service" value="vault"/>
         * <param name="module" value="toto"/>
         * <param name="code" value="totocode"/>
         * </feature>
         */
        xmlBuilder.append("<feature spec=\"" + f.getType() + "\">").append(Config.getLineSeparator());
        String tab = "  ";
        for (Entry<String, Object> p : f.getResolvedParams().entrySet()) {
            xmlBuilder.append(tab + "<param name=\"" + p.getKey() + "\"" + " value=\"" + p.getValue() + "\"/>").append(Config.getLineSeparator());
        }
        xmlBuilder.append("</feature>").append(Config.getLineSeparator());
        session.println(xmlBuilder.toString());
        session.println("Unset parameters");
        for (String p : f.getUndefinedParams()) {
            session.println(tab + "<param name=\"" + p + "\"" + " value=\"???\"/>");
        }
    }

    private static void displayFeatureSpec(PmCommandInvocation session, Group grp) throws IOException {
        FeatureSpecInfo f = grp.getSpec();
        session.println("Feature type       : " + f.getSpecId().getName());
        session.println("Feature origin     : " + f.getSpecId().getGav());
        session.println("Feature description: " + f.getDescription());
        if (!f.isEnabled()) {
            session.println("WARNING! The feature is not enabled.");
            session.println("Missing packages");
            for (Identity m : f.getMissingPackages()) {
                session.println(m.toString());
            }
        }
        List<FeatureParameterSpec> idparams = f.getSpec().getIdParams();
        String tab = "  ";
        session.println("Feature Id parameters");
        if (idparams.isEmpty()) {
            session.println("NONE");
        } else {
            for (FeatureParameterSpec param : idparams) {
                StringBuilder builder = new StringBuilder();
                builder.append(tab + param.getName()).append(Config.getLineSeparator());
                builder.append(tab + tab + "description  : " + "no description available").append(Config.getLineSeparator());
                builder.append(tab + tab + "type         : " + param.getType()).append(Config.getLineSeparator());
                builder.append(tab + tab + "default-value: " + param.getDefaultValue()).append(Config.getLineSeparator());
                builder.append(tab + tab + "nillable     : " + param.isNillable()).append(Config.getLineSeparator());
                session.print(builder.toString());
            }
        }
        // Add spec parameters
        session.println("Feature parameters");
        Map<String, FeatureParameterSpec> params = f.getSpec().getParams();
        if (params.isEmpty()) {
            session.println("NONE");
        } else {
            for (Entry<String, FeatureParameterSpec> entry : params.entrySet()) {
                FeatureParameterSpec param = entry.getValue();
                if (!param.isFeatureId()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(tab + param.getName()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "description  : " + "no description available").append(Config.getLineSeparator());
                    builder.append(tab + tab + "type         : " + param.getType()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "default-value: " + param.getDefaultValue()).append(Config.getLineSeparator());
                    builder.append(tab + tab + "nillable     : " + param.isNillable()).append(Config.getLineSeparator());
                    session.println(builder.toString());
                }
            }
        }

        session.println(Config.getLineSeparator() + "Packages (content)");
        if (f.getPackages().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (PackageInfo p : f.getPackages()) {
                session.println(p.getIdentity().toString());
            }

        }
        session.println(Config.getLineSeparator() + "JBOSS Modules (content)");
        if (f.getModules().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (PackageInfo p : f.getModules()) {
                session.println(p.getIdentity().toString());
            }

        }
        session.println(Config.getLineSeparator() + "Provided capabilities");
        if (f.getSpec().getProvidedCapabilities().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (CapabilitySpec c : f.getSpec().getProvidedCapabilities()) {
                session.println(tab + c.toString());
            }
        }
        session.println(Config.getLineSeparator() + "Consumed capabilities");
        if (f.getSpec().getRequiredCapabilities().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (CapabilitySpec c : f.getSpec().getRequiredCapabilities()) {
                session.println(tab + c.toString());
            }
        }
        session.println(Config.getLineSeparator() + "Features dependencies");
        if (f.getSpec().getFeatureDeps().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureDependencySpec c : f.getSpec().getFeatureDeps()) {
                session.println(tab + c.getFeatureId().toString());
            }
        }
        session.println(Config.getLineSeparator() + "Features references");
        if (f.getSpec().getFeatureRefs().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureReferenceSpec c : f.getSpec().getFeatureRefs()) {
                session.println(tab + c.getFeature());
            }
        }
        session.println(Config.getLineSeparator() + "Features Annotations");
        if (f.getSpec().getAnnotations().isEmpty()) {
            session.println(tab + "NONE");
        } else {
            for (FeatureAnnotation c : f.getSpec().getAnnotations()) {
                session.println(tab + c.toString());
            }
        }
    }

    private static void displayModule(PmCommandInvocation session, Group grp) throws IOException {
        PackageInfo pkg = grp.getPackage();
        session.println("Module name : " + pkg.getIdentity().getName());
        session.println("Module origin : " + pkg.getIdentity().getOrigin());
        session.println("Module version : " + (pkg.getModuleVersion() == null ? "none" : pkg.getModuleVersion()));
        session.println("Module providers (features that depend on this module)");
        if (pkg.getProviders().isEmpty()) {
            session.println("default provider");
        } else {
            for (Identity id : pkg.getProviders()) {
                session.println(id.toString());
            }
        }
        session.println("Module dependencies");
        if (grp.getGroups().isEmpty()) {
            session.println("NONE");
        } else {
            for (Group dep : grp.getGroups()) {
                session.println(dep.getIdentity().toString());
            }
        }
        session.println("Module artifacts gav");
        if (pkg.getArtifacts().isEmpty()) {
            session.println("NONE");
        } else {
            for (String art : pkg.getArtifacts()) {
                session.println(art);
            }
        }
    }

    private static void displayPackage(PmCommandInvocation session, Group grp) throws IOException {
        PackageInfo pkg = grp.getPackage();
        session.println("Package name : " + pkg.getIdentity().getName());
        session.println("Package origin : " + pkg.getIdentity().getOrigin());
        session.println("Package providers (features that depend on this package)");
        if (pkg.getProviders().isEmpty()) {
            session.println("default provider");
        } else {
            for (Identity id : pkg.getProviders()) {
                session.println(id.toString());
            }
        }
        session.println("Package dependencies");
        if (grp.getGroups().isEmpty()) {
            session.println("NONE");
        } else {
            for (Group dep : grp.getGroups()) {
                session.println(dep.getIdentity().getName());
            }
            for (String o : pkg.getSpec().getPackageOrigins()) {
                for (PackageDependencySpec p : pkg.getSpec().getExternalPackageDeps(o)) {
                    session.println(o + "#" + p.getName());
                }
            }
        }
        session.println("Package content");
        if (pkg.getContent().isEmpty()) {
            session.println("NONE");
        } else {
            StringBuilder contentBuilder = new StringBuilder();
            for (String name : pkg.getContent()) {
                contentBuilder.append("  " + name + Config.getLineSeparator());
            }
            session.println(contentBuilder.toString());
        }
    }
}
