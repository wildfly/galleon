/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
