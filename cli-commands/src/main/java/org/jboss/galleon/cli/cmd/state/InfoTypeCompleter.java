/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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


import java.util.Arrays;
import java.util.List;

import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.PmCompleterInvocation;

/**
 *
 * @author jdenise@redhat.com
 */
public class InfoTypeCompleter extends AbstractCompleter {

    public static final String CONFIGS = "configs";
    public static final String DEPENDENCIES = "dependencies";
    public static final String OPTIONS = "options";

    public static final String ALL = "all";
    public static final String PATCHES = "patches";
    public static final String LAYERS = "layers";
    public static final String OPTIONAL_PACKAGES = "optional-packages";
    public static final String UNIVERSES = "universes";

    @Override
    protected List<String> getItems(PmCompleterInvocation completerInvocation) {
        return Arrays.asList(ALL, CONFIGS, DEPENDENCIES, OPTIONAL_PACKAGES, OPTIONS, LAYERS, PATCHES, UNIVERSES);
    }

}
