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
package org.jboss.galleon.layout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeaturePackSpec.Family;
import org.jboss.galleon.universe.Channel;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;

/**
 * Family handling.
 *
 * @author jdenise
 */
class FeaturePackFamily {

    private static class FamilyMapping {

        private final Family family;
        private final FeaturePackLocation.FPID fpid;

        FamilyMapping(Family family, FeaturePackLocation.FPID fpid) {
            this.family = family;
            this.fpid = fpid;
        }
    }

    interface FeaturePackSpecResolver {

        FeaturePackSpec resolve(FeaturePackLocation loc) throws ProvisioningException;
    }

    static class FamilyResolutionResult {

        private final FeaturePackConfig memberDependency;
        private final boolean differentFamilyMember;
        private final FeaturePackConfig originalDependency;

        private FamilyResolutionResult(FeaturePackConfig originalDependency, FeaturePackConfig memberDependency, boolean differentFamilyMember) {
            this.originalDependency = originalDependency;
            this.memberDependency = memberDependency;
            this.differentFamilyMember = differentFamilyMember;
        }

        boolean isDifferentMember() {
            return differentFamilyMember;
        }

        FeaturePackConfig getResolvedDependency() {
            return memberDependency;
        }

        FeaturePackConfig getOriginalDependency() {
            return originalDependency;
        }
    }

    class FeaturePackFamilyResolution {

        private final FeaturePackSpecResolver resolver;
        private final FPID childFPID;
        private FPID childMavenFPID;
        private final FeaturePackSpec fpSpec;
        FeaturePackFamilyResolution(FeaturePackSpec fpSpec, FeaturePackLocation fpl, FeaturePackSpecResolver resolver) {
            this.fpSpec = fpSpec;
            this.resolver = resolver;
            this.childFPID = fpl.getFPID();
        }

        void resolutionDone() throws ProvisioningException {
            registerFamilyMember(fpSpec.getFamily(), childFPID);
        }

        FamilyResolutionResult resolveDependency(FeaturePackConfig originalDependency) throws ProvisioningException {
            FPID memberFPID = originalDependency.getLocation().getFPID();
            FeaturePackConfig memberDependency = originalDependency;
            boolean differentFamilyMember = false;
            String allowedFamily = originalDependency.getAllowedFamily();
            if (hasFamilyMembers()) {
                if (allowedFamily != null) {
                    FPID member = getDependencyMember(Family.fromString(allowedFamily));
                    // Ensure that the child that inherit criteria is not selected.
                    if (member != null) {
                        if (childMavenFPID == null) {
                            childMavenFPID = toMavenLocation(childFPID);
                        }
                        if (!member.equals(childMavenFPID) && !originalDependency.getLocation().getProducer().equals(member.getProducer())) {
                            //System.out.println("\nFound a family member from " + allowedFamily);
                            //System.out.println("Provisioning time dep : " + member);
                            //System.out.println("Build time dep        : " + originalDependency.getLocation().getProducer());
                            memberFPID = member;
                            memberDependency = FeaturePackConfig.builder(memberFPID.getLocation()).init(originalDependency).build();
                            differentFamilyMember = true;
                        }
                    }
                }
            }
            FeaturePackSpec depSpec = resolver.resolve(memberFPID.getLocation());
            if (!differentFamilyMember) {
                // If the feature-pack dependency has a family, and a member already exists, make sure that the dependency matches the current member.
                // otherwise it means that a different member is provisioned but this feature-pack expects a given member.
                // Just use the family name, no criteria to retrieve any member of this family
                FPID existingMember = depSpec.getFamily() == null ? null : getDependencyMember(depSpec.getFamily());
                if (existingMember != null) {
                    if (!existingMember.getProducer().equals(originalDependency.getLocation().getProducer())) {
                        if (allowedFamily == null) {
                            throw new ProvisioningException("The feature-pack " + childFPID + " expects the dependency on " + originalDependency.getLocation()
                                    + " but this dependency is in the family " + depSpec.getFamily() + " for which a different member "
                                    + existingMember + " is in use.");
                        }
                    }
                }
            }
            registerFamilyMember(depSpec.getFamily(), memberFPID);
            return new FamilyResolutionResult(originalDependency, memberDependency, differentFamilyMember);
        }

    }
    private final Map<String, FamilyMapping> resolvedFamilyMembers = new HashMap<>();
    private final ProvisioningLayoutFactory factory;

    FeaturePackFamily(ProvisioningLayoutFactory factory) {
        this.factory = factory;

    }

    private void registerFamilyMember(Family family, FPID member) {
        if (family != null) {
            String key = family.getMemberFamilyID();
            if (!resolvedFamilyMembers.containsKey(key)) {
                resolvedFamilyMembers.put(key, new FamilyMapping(family, toMavenLocation(member)));
            }
        }
    }

    private FPID toMavenLocation(FPID original) {
        if (!original.getLocation().isMavenCoordinates()) {
            // System.out.println("Not maven location " + original);
            try {
                Channel channel = factory.getUniverseResolver().getChannel(original.getLocation());
                original = getMavenCoordinates(channel, original);
                // System.out.println("Transformed to maven " + original);
            } catch(ProvisioningException ex) {
                // Ok, no universe available, keep original.
            }
        }
        return original;
    }

    private FPID getDependencyMember(Family family) {
        if (family != null) {
            // Do we have a member that implements the criteria without inheriting them?
            // That is required to not return a child feature-pack instead of the parent.
            for (FamilyMapping mapping : resolvedFamilyMembers.values()) {
                if (mapping.family.getName().equals(family.getName()) && mapping.family.getLocalCriteria().containsAll(family.getCriteria())) {
                    return mapping.fpid;
                }
            }
            for (FamilyMapping mapping : resolvedFamilyMembers.values()) {
                if (mapping.family.getName().equals(family.getName()) && mapping.family.getCriteria().containsAll(family.getCriteria())) {
                    return mapping.fpid;
                }
            }
        }
        return null;
    }

    private boolean hasFamilyMembers() {
        return !resolvedFamilyMembers.isEmpty();
    }

    FeaturePackFamilyResolution newResolution(FeaturePackSpec fpSpec, FeaturePackLocation fpl, FeaturePackSpecResolver resolver) throws ProvisioningException {
        return new FeaturePackFamilyResolution(fpSpec, fpl, resolver);
    }

    void validateFamilies() throws ProvisioningException {
        Map<String, Map<String, List<FPID>>> implementors = new HashMap<>();
        Set<String> families = new TreeSet<>();
        for (FamilyMapping mapping : resolvedFamilyMembers.values()) {
            Map<String, List<FPID>> perFamily = implementors.get(mapping.family.getName());
            if(perFamily == null) {
                perFamily = new HashMap<>();
                implementors.put(mapping.family.getName(), perFamily);
            }
            families.add(mapping.family.getName());
            for (Family.Criteria c : mapping.family.getCriteria()) {
                if (!c.isInherited()) {
                    List<FPID> impl = perFamily.get(c.getName());
                    if (impl == null) {
                        impl = new ArrayList<>();
                        perFamily.put(c.getName(), impl);
                    }
                    impl.add(mapping.fpid);
                }
            }
        }
        StringBuilder error = new StringBuilder();
        for(Entry<String, Map<String, List<FPID>>> entry : implementors.entrySet()) {
            Map<String, List<FPID>> fam = entry.getValue();
            for(Entry<String, List<FPID>> impl : fam.entrySet()) {
                List<FPID> lst = impl.getValue();
                if(lst.size() > 1) {
                    error.append(entry.getKey()).append(":").append(impl.getKey()).
                            append(" is provided by more than 1 feature-pack ").
                            append(lst).append(".\n");
                }
            }
        }
        if(families.size() > 1) {
            error.append("Different families are mixed in the provisioning configuration: ").append(families).append("\n");
        }
        if(!error.isEmpty()) {
            throw new ProvisioningException("Some errors have been encountered processing feature-pack families.\n" + error.toString() +
                    "Check the config and remove the conflicting feature-packs.");
        }
    }

    private static final String GET_FEATURE_PACK_GROUP_ID = "getFeaturePackGroupId";
    private static final String GET_FEATURE_PACK_ARTIFACT_ID = "getFeaturePackArtifactId";
    private static FPID getMavenCoordinates(Channel channel, FPID original) {
        try {
            Method groupIdMethod = channel.getClass().getDeclaredMethod(GET_FEATURE_PACK_GROUP_ID);
            Method artifactIdMethod = channel.getClass().getDeclaredMethod(GET_FEATURE_PACK_ARTIFACT_ID);
            if (groupIdMethod != null && artifactIdMethod != null) {
                String groupId = (String) groupIdMethod.invoke(channel);
                String artifactId = (String) artifactIdMethod.invoke(channel);
                original = FeaturePackLocation.fromString(groupId + ":" + artifactId + ":" + original.getBuild()).getFPID();
            }
        } catch (Exception ex) {
            // XXX OK.
        }
        return original;
    }
}
