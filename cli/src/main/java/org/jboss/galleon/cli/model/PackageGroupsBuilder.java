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
package org.jboss.galleon.cli.model;

import static org.jboss.galleon.cli.model.FeatureContainer.ROOT;

import java.util.HashMap;
import java.util.Map;

import org.jboss.galleon.spec.PackageDependencySpec;

/**
 *
 * @author jdenise@redhat.com
 */
public class PackageGroupsBuilder {

    public interface PackageInfoBuilder {

        PackageInfo build(Identity identity);
    }

    private final Map<Identity, Group> allPackagesGroups = new HashMap<>();

    private Group packagesRoot;
    private Group modulesRoot;

    PackageGroupsBuilder() {
        resetRoots();
        allPackagesGroups.put(packagesRoot.getIdentity(), packagesRoot);
    }

    final void resetRoots() {
        packagesRoot = Group.fromString(null, ROOT);
        modulesRoot = Group.fromString(null, ROOT);
    }

    Group getPackagesRoot() {
        return packagesRoot;
    }

    Group getModulesRoot() {
        return modulesRoot;
    }

    Map<Identity, Group> getPackages() {
        return allPackagesGroups;
    }

    void buildGroups(PackageInfo pkg, PackageInfoBuilder builder, String origin) {
        buildGroups(packagesRoot, pkg, builder, origin);
    }

    private void buildGroups(Group grp, PackageInfo pkg, PackageInfoBuilder builder, String origin) {
        Group gp = allPackagesGroups.get(pkg.getIdentity());
        if (gp == null) {
            gp = Group.fromIdentity(pkg.getIdentity());
            allPackagesGroups.put(pkg.getIdentity(), gp);
            gp.setPackage(pkg);

            for (PackageDependencySpec s : pkg.getSpec().getLocalPackageDeps()) {
                buildGroups(gp, builder.build(Identity.fromString(origin, s.getName())), builder, origin);
            }
            for (String o : pkg.getSpec().getPackageOrigins()) {
                for (PackageDependencySpec p : pkg.getSpec().getExternalPackageDeps(o)) {
                    buildGroups(gp, builder.build(Identity.fromString(o, p.getName())), builder, o);
                }
            }
        }
        grp.addGroup(gp);
    }
}
