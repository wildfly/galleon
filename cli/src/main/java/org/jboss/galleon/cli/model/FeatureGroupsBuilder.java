/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
import java.util.Set;

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
            String[] subs = g.split("_");
            if (subs.length > 1) { // complex features.
                for (int i = 0; i < subs.length; i++) {
                    String value = (String) id.getParams().get(subs[i]);
                    if (value != null) {
                        current = addGroup(value, current, pathItems, subs[i]);
                    } else {
                        current = addGroup(current, pathItems, subs[i]);
                    }
                }
            } else {
                String value = (String) id.getParams().get(g);
                if (value != null) {
                    current = addGroup(value, current, pathItems, g);
                } else {
                    // Not a naming node.
                }
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
        Set<String> params = info.getAllParameters();
        for (int it = 0; it < groups.length; it++) {
            String g = groups[it];
            String[] subs = g.split("_");
            if (subs.length > 1) {
                // Simply add nodes, no more naming
                for (int i = 0; i < subs.length; i++) {
                    current = addGroup(current, pathItems, subs[i]);
                }
            } else {
//            else if (it == groups.length - 1) { // we are at the end. add a node
//                current = addGroup(current, pathItems, g);
//            } else {
//                boolean containsParam = params.contains(g);
//                if (containsParam) {
//                    if (it == groups.length - 1) {
//                        current = addGroup(current, pathItems, g);
//                    } else {
//                        // The next in groups can contain '_', must be removed
//                        String[] filtered = groups[it + 1].split("_");
//                        String n = filtered[0];
//                        if (params.contains(n)) {
//                            // The next is a param, not a value, add a group.
//                            current = addGroup(current, pathItems, g);
//                        } else {
//                            it = it + 1;
//                            current = addGroup(n, current, pathItems, g, wildflyModel);
//                            // Then all remaining complex
//                            for (int j = 1; j < filtered.length; j++) {
//                                current = addGroup(current, pathItems, filtered[j]);
//                            }
//                        }
//                    }
//                } else {
                current = addGroup(current, pathItems, g);
                //}
            }
        }
        return current;
    }
}
