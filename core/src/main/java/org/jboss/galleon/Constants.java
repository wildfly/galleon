/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon;

/**
 *
 * @author Alexey Loubyansky
 */
public interface Constants {

    String DOT_GLNEW = ".glnew";
    String DOT_XML = ".xml";
    String CONFIGS = "configs";
    String CONFIG_XML = "config.xml";
    String CONTENT = "content";
    String FEATURE_GROUPS = "feature_groups";
    String FEATURES = "features";
    String FEATURE_PACK_XML = "feature-pack.xml";
    String HASHES = "hashes";
    String HISTORY = "history";
    String HISTORY_LIST = "list";
    String KEEP = "keep";
    String LAYERS = "layers";
    String LAYER_SPEC_XML = "layer-spec.xml";
    String MAVEN = "maven";
    String MODEL_XML = "model.xml";
    String MODULES_XML = "module.xml";
    String PACKAGE_XML = "package.xml";
    String PACKAGES = "packages";
    String PLUGINS = "plugins";
    String PROVISIONING_XML = "provisioning.xml";
    String REMOVE = "remove";
    String RESOURCES = "resources";
    String SPEC_XML = "spec.xml";
    String THIS = "this";
    String UNDO_TASKS = "undo.tasks";
    String ZIP = "zip";

    String PROVISIONED_STATE_DIR = ".galleon";
    String PROVISIONED_STATE_XML = "provisioned.xml";

    String GLN_UNDEFINED = "GLN_UNDEFINED";

    String FALSE = "false";
    String TRUE = "true";

    // BUILT-IN TYPES
    String BUILT_IN_TYPE_STRING = "String";

    // SYSTEM PROPERTIES
    String PROP_CONFIG_ARRANGER = "galleon.config.arranger";

    // CONFIG ARRANGERS
    String CONFIG_ARRANGER_SPEC_ONLY = "spec-only";

    // OPTIONS
    String OPTIONAL_PACKAGES = "optional-packages";
    String ALL = "all";
    String NONE = "none";
    String PASSIVE = "passive";
    String PASSIVE_PLUS = "passive+";

    String VERSION_CONVERGENCE = "version-convergence";
    String FIRST_PROCESSED = "first-processed";
    String FAIL = "fail";

}
