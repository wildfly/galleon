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
import java.util.List;
import java.util.Map;

import org.jboss.galleon.runtime.ResolvedFeatureId;

/**
 *
 * @author jdenise@redhat.com
 */
public class FeatureGroupsBuilder {

    private final Map<String, Group> allgroups = new HashMap<>();
    private final Group root;
    private String currentPath;

    FeatureGroupsBuilder() {
        root = Group.fromString("", ROOT);
        allgroups.put(root.getIdentity().getName(), root);
    }

    Group getRoot() {
        return root;
    }

    private Group addGroup(String value, Group current, List<String> pathItems, String grpName) {
        if (value != null) {
            current = addGroup(current, pathItems, grpName);
            current = addGroup(current, pathItems, value);
        }
        return current;
    }

    private Group addGroup(Group current, List<String> pathItems, String grpName) {
        Group fg = allgroups.get(currentPath + grpName);
        if (fg == null) {
            fg = Group.fromString("", grpName);
            allgroups.put(currentPath + grpName, fg);
        }
        current.addGroup(fg);
        currentPath += fg.getIdentity().getName() + "/";
        pathItems.add(fg.getIdentity().getName());
        return fg;
    }

    Group buildFeatureGroups(String name, ResolvedFeatureId id, List<String> pathItems) {
        String[] groups = name.split("\\.");
        Group current = allgroups.get(ROOT);
        currentPath = "/";
        for (int it = 0; it < groups.length; it++) {
            String g = groups[it];
            String value = (String) id.getParams().get(g);
            if (value != null) {
                current = addGroup(value, current, pathItems, g);
                if (it < groups.length - 1) {
                    String next = groups[it + 1];
                    if (next.equals(value)) {
                        it += 1;
                    }
                }
            } else {
                current = addGroup(current, pathItems, g);
            }
        }
        return current;
    }

    /**
     *
     */
    Group buildFeatureSpecGroups(String name, FeatureSpecInfo info,
            boolean wildflyModel, List<String> pathItems) {
        String[] groups = name.split("\\.");
        Group current = allgroups.get(ROOT);
        currentPath = "/";
        for (int it = 0; it < groups.length; it++) {
            String g = groups[it];
            current = addGroup(current, pathItems, g);
        }
        return current;
    }
}
