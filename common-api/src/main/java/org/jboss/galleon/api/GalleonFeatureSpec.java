/*
 * Copyright 2016-2024 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jdenise
 */
public class GalleonFeatureSpec {

    private final String name;
    private final String stability;
    private final List<GalleonFeatureParamSpec> params = new ArrayList<>();

    public GalleonFeatureSpec(String name, String stability) {
        this.name = name;
        this.stability = stability;
    }

    public void addParam(GalleonFeatureParamSpec param) {
        params.add(param);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the stability
     */
    public String getStability() {
        return stability;
    }

    /**
     * @return the params
     */
    public List<GalleonFeatureParamSpec> getParams() {
        return Collections.unmodifiableList(params);
    }
}
