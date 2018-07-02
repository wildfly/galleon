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

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Can contain other groups, spec, feature or package.
 *
 * @author jdenise@redhat.com
 */
public class Group implements Comparable<Group> {

    private final Set<Group> groups = new TreeSet<>();
    private FeatureSpecInfo featureSpec;
    private FeatureInfo feature;
    private PackageInfo pkg;
    private Group previous;
    private final Identity id;

    private Group(Identity id) {
        this.id = id;
    }

    static Group fromIdentity(Identity id) {
        return new Group(id);
    }

    public static Group fromString(String origin, String name) {
        return new Group(Identity.fromString(origin == null ? "" : origin, name));
    }

    public Identity getIdentity() {
        return id;
    }

    public void setPrevious(Group previous) {
        this.previous = previous;
    }

    public Group getPrevious() {
        return previous;
    }

    public void addGroup(Group fg) {
        groups.add(fg);
        fg.setPrevious(this);
    }

    public void setPackage(PackageInfo pkg) {
        this.pkg = pkg;
    }

    public PackageInfo getPackage() {
        return pkg;
    }

    public void setFeatureSpec(FeatureSpecInfo f) {
        this.featureSpec = f;
        f.setName(getIdentity().getName());
    }

    public void setFeature(FeatureInfo f) {
        this.feature = f;
    }

    public FeatureInfo getFeature() {
        return feature;
    }

    public FeatureSpecInfo getSpec() {
        return featureSpec;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Group)) {
            return false;
        }
        Group ofg = (Group) other;
        return getIdentity().equals(ofg.getIdentity());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public int compareTo(Group o) {
        return getIdentity().compareTo(o.getIdentity());
    }
}
